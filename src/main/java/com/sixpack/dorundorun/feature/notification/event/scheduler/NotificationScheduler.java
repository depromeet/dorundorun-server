package com.sixpack.dorundorun.feature.notification.event.scheduler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.notification.domain.ScheduledNotificationData;
import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis Sorted Set 기반 예약 알림 스케줄러
 *
 * 역할:
 * 1. 매분마다 Redis Sorted Set에서 예약된 알림 조회
 * 2. 현재 시간이 도래한 알림 처리
 * 3. Redis Stream으로 이벤트 재발행
 * 4. Redis에서 처리된 알림 제거
 *
 * 예약 시간:
 * - 매분 정각 (1분마다 체크)
 * - 지연: 최대 1분 이내
 *
 * Redis 구조:
 * - Sorted Set: "pending-notifications"
 *   ├─ member: eventId (UUID)
 *   └─ score: 예약 시간 (Unix timestamp)
 *
 * - Hash: "notifications"
 *   ├─ field: eventId
 *   └─ value: JSON 직렬화된 ScheduledNotificationData
 *
 * 예약 알림 종류:
 * - CERTIFICATION_REMINDER: 러닝 시작 후 23시간
 * - RUNNING_PROGRESS_REMINDER: 마지막 러닝 후 7일
 * - NEW_USER_RUNNING_REMINDER: 가입 후 24시간
 * - NEW_USER_FRIEND_REMINDER: 가입 후 48시간
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;
	private final RedisStreamPublisher redisStreamPublisher;

	/**
	 * 매분 실행: Redis Sorted Set에서 예약된 알림을 조회하고 처리
	 *
	 * 동작 방식:
	 * 1. ZRANGEBYSCORE로 현재 시간 이전의 모든 알림 조회
	 *    - O(log N + M) 시간복잡도로 매우 빠름
	 * 2. 각 알림 ID에 대해 Hash에서 이벤트 데이터 복원
	 * 3. 원본 이벤트를 Redis Stream으로 재발행
	 * 4. 처리 완료 후 Sorted Set과 Hash에서 제거
	 *
	 * 보장 사항:
	 * - 최대 1분 이내 지연 (매분 정각 실행)
	 * - 정확도: 분 단위 (초 단위 정확도 불필요)
	 * - 중복 방지: 처리 후 Redis에서 제거되므로 자동 방지
	 * - 실패 복구: 재시도는 Redis Stream 메커니즘에 의존
	 */
	@Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
	public void processScheduledNotifications() {
		log.debug("Starting scheduled notification processor");

		try {
			// 현재 시간 (Unix timestamp)
			long now = Instant.now().getEpochSecond();

			// Sorted Set에서 score가 now 이하인 모든 항목 조회
			// O(log N + M) 시간복잡도
			Set<String> eventIds = redisTemplate.opsForZSet()
				.rangeByScore("pending-notifications", 0, now);

			if (eventIds == null || eventIds.isEmpty()) {
				log.debug("No scheduled notifications to process");
				return;
			}

			log.info("Processing {} scheduled notifications", eventIds.size());

			int successCount = 0;
			int failureCount = 0;

			for (String eventId : eventIds) {
				try {
					// Hash에서 이벤트 데이터 조회
					Object eventData = redisTemplate.opsForHash().get("notifications", eventId);

					if (eventData == null) {
						log.warn("Scheduled notification data not found: eventId={}", eventId);
						// 데이터 없으면 Sorted Set에서만 제거
						redisTemplate.opsForZSet().remove("pending-notifications", eventId);
						failureCount++;
						continue;
					}

					// JSON으로부터 ScheduledNotificationData 복원
					ScheduledNotificationData scheduledData = objectMapper.readValue(
						eventData.toString(),
						ScheduledNotificationData.class
					);

					// 원본 이벤트를 Redis Stream으로 재발행
					PushNotificationRequestedEvent pushEvent = PushNotificationRequestedEvent.builder()
						.recipientUserId(scheduledData.getUserId())
						.notificationType(scheduledData.getNotificationType())
						.relatedId(String.valueOf(scheduledData.getUserId()))
						.metadata(new HashMap<>())
						.build();
					redisStreamPublisher.publishAfterCommit(pushEvent);

					log.info("Published scheduled notification: eventId={}, type={}, userId={}, scheduledAt={}",
						eventId, scheduledData.getNotificationType(), scheduledData.getUserId(),
						scheduledData.getScheduledAt());

					// 처리 완료: Sorted Set과 Hash에서 제거
					redisTemplate.opsForZSet().remove("pending-notifications", eventId);
					redisTemplate.opsForHash().delete("notifications", eventId);

					successCount++;

				} catch (Exception e) {
					log.error("Failed to process scheduled notification: eventId={}", eventId, e);
					failureCount++;
					// 개별 실패가 전체 배치를 막지 않도록 계속 진행
					try {
						// 에러가 발생한 경우에도 Redis에서 제거
						// (무한 루프 방지)
						redisTemplate.opsForZSet().remove("pending-notifications", eventId);
						redisTemplate.opsForHash().delete("notifications", eventId);
					} catch (Exception removeError) {
						log.error("Failed to remove corrupted notification: eventId={}", eventId, removeError);
					}
				}
			}

			if (failureCount > 0) {
				log.warn("Scheduled notification processing completed: success={}, failure={}, total={}",
					successCount, failureCount, eventIds.size());
			} else {
				log.info("Scheduled notification processing completed: success={}, total={}",
					successCount, eventIds.size());
			}

		} catch (Exception e) {
			log.error("Critical error in scheduled notification processor", e);
		}
	}
}
