package com.sixpack.dorundorun.feature.notification.event.scheduler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.friend.dao.FriendJpaRepository;
import com.sixpack.dorundorun.feature.notification.domain.ScheduledNotificationData;
import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;
import com.sixpack.dorundorun.feature.run.dao.RunSessionJpaRepository;
import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;
	private final RedisStreamPublisher redisStreamPublisher;
	private final FeedJpaRepository feedJpaRepository;
	private final RunSessionJpaRepository runSessionJpaRepository;
	private final UserJpaRepository userJpaRepository;
	private final FriendJpaRepository friendJpaRepository;

	@Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
	public void processScheduledNotifications() {
		log.debug("Starting scheduled notification processor");

		try {
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

					// FEED_REMINDER: 게시물 업로드 여부 확인
					if ("FEED_REMINDER".equals(scheduledData.getNotificationType())) {
						// 메타데이터에서 runSessionId 확인
						Object runSessionIdObj = scheduledData.getAdditionalData() != null ?
							scheduledData.getAdditionalData().get("runSessionId") : null;

						if (runSessionIdObj != null) {
							Long runSessionId = Long.parseLong(runSessionIdObj.toString());

							// 해당 러닝 세션의 게시물 여부 확인
							boolean feedExists = feedJpaRepository.findByRunSessionIdAndDeletedAtIsNull(runSessionId)
								.isPresent();

							if (feedExists) {
								log.info("Feed already uploaded for runSessionId: {}, skipping notification",
									runSessionId);
								// 게시물이 이미 있으면 알림 발송 안 함
								// Redis에서만 제거
								redisTemplate.opsForZSet().remove("pending-notifications", eventId);
								redisTemplate.opsForHash().delete("notifications", eventId);
								successCount++;
								continue;
							}
						}
					}

					// RUNNING_PROGRESS_REMINDER: 신규 러닝 여부 확인
					if ("RUNNING_PROGRESS_REMINDER".equals(scheduledData.getNotificationType())) {
						// 메타데이터에서 runSessionId 확인
						Object runSessionIdObj = scheduledData.getAdditionalData() != null ?
							scheduledData.getAdditionalData().get("runSessionId") : null;

						if (runSessionIdObj != null) {
							Long runSessionId = Long.parseLong(runSessionIdObj.toString());

							// 해당 러닝 세션 이후 신규 러닝이 있는지 확인
							boolean hasNewRun = runSessionJpaRepository.existsNewRunAfter(runSessionId);

							if (hasNewRun) {
								log.info("New run exists after runSessionId: {}, skipping notification",
									runSessionId);
								// 신규 러닝이 있으면 알림 발송 안 함
								// Redis에서만 제거
								redisTemplate.opsForZSet().remove("pending-notifications", eventId);
								redisTemplate.opsForHash().delete("notifications", eventId);
								successCount++;
								continue;
							}
						}
					}

					// NEW_USER_RUNNING_REMINDER: 러닝 여부 확인
					if ("NEW_USER_RUNNING_REMINDER".equals(scheduledData.getNotificationType())) {
						Long userId = scheduledData.getUserId();

						// 해당 사용자의 완료된 러닝이 있는지 확인
						boolean hasAnyRun = runSessionJpaRepository.existsByUserIdAndFinishedAtIsNotNull(userId);

						if (hasAnyRun) {
							log.info("User has already completed a run: userId={}, skipping notification", userId);
							// 러닝이 있으면 알림 발송 안 함
							// Redis에서만 제거
							redisTemplate.opsForZSet().remove("pending-notifications", eventId);
							redisTemplate.opsForHash().delete("notifications", eventId);
							successCount++;
							continue;
						}
					}

					// NEW_USER_FRIEND_REMINDER: 친구 여부 확인
					if ("NEW_USER_FRIEND_REMINDER".equals(scheduledData.getNotificationType())) {
						Long userId = scheduledData.getUserId();

						// 해당 사용자가 추가한 친구가 있는지 확인
						boolean hasFriends = friendJpaRepository.existsByUserIdAndDeletedAtIsNull(userId);

						if (hasFriends) {
							log.info("User has already added friends: userId={}, skipping notification", userId);
							// 친구가 있으면 알림 발송 안 함
							// Redis에서만 제거
							redisTemplate.opsForZSet().remove("pending-notifications", eventId);
							redisTemplate.opsForHash().delete("notifications", eventId);
							successCount++;
							continue;
						}
					}

					// 원본 이벤트를 Redis Stream으로 재발행
					PushNotificationRequestedEvent pushEvent = PushNotificationRequestedEvent.builder()
						.recipientUserId(scheduledData.getUserId())
						.notificationType(scheduledData.getNotificationType())
						.relatedId(null)
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
