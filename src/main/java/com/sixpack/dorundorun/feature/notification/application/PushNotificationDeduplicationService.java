package com.sixpack.dorundorun.feature.notification.application;

import java.time.Duration;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushNotificationDeduplicationService {

	private static final String DEDUP_KEY_PREFIX = "push:dedup:";

	private static final Map<String, Duration> TTL_BY_TYPE = Map.of(
		"CHEER_FRIEND", Duration.ofHours(1),
		"FEED_UPLOADED", Duration.ofHours(24),
		"FEED_REACTION", Duration.ofHours(1),
		"FEED_REMINDER", Duration.ofHours(25),
		"RUNNING_PROGRESS_REMINDER", Duration.ofDays(8),
		"NEW_USER_RUNNING_REMINDER", Duration.ofDays(8),
		"NEW_USER_FRIEND_REMINDER", Duration.ofDays(4)
	);

	private static final Duration DEFAULT_TTL = Duration.ofHours(24);

	private final StringRedisTemplate redisTemplate;

	/**
	 * 중복 체크 및 락 획득 시도
	 * @return true면 처리 가능 (신규), false면 중복
	 */
	public boolean tryAcquireLock(PushNotificationRequestedEvent event) {
		String dedupKey = generateDedupKey(event);
		Duration ttl = getTtlForType(event.notificationType());

		try {
			Boolean acquired = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", ttl);

			if (acquired == null) {
				// Redis 오류로 null 반환 시 fail-open (알림 발송 허용)
				log.warn("Redis returned null for dedup check, proceeding with notification: key={}", dedupKey);
				return true;
			}

			if (acquired) {
				log.debug("Dedup lock acquired: key={}", dedupKey);
				return true;
			} else {
				log.info("Duplicate notification detected, skipping: key={}", dedupKey);
				return false;
			}
		} catch (Exception e) {
			// Redis 연결 오류 시 fail-open (알림 발송 허용)
			log.warn("Redis error during dedup check, proceeding with notification: key={}", dedupKey, e);
			return true;
		}
	}

	/**
	 * 이벤트로부터 중복 방지 키 생성
	 */
	public String generateDedupKey(PushNotificationRequestedEvent event) {
		// idempotencyKey가 명시적으로 설정된 경우 이를 우선 사용
		if (event.idempotencyKey() != null && !event.idempotencyKey().isEmpty()) {
			return DEDUP_KEY_PREFIX + event.idempotencyKey();
		}

		StringBuilder keyBuilder = new StringBuilder(DEDUP_KEY_PREFIX)
			.append(event.notificationType())
			.append(":")
			.append(event.recipientUserId());

		if (event.relatedId() != null && !event.relatedId().isEmpty()) {
			keyBuilder.append(":").append(event.relatedId());
		}

		// CHEER_FRIEND의 경우 응원한 사용자 ID도 추가 (동일인이 같은 사람에게 연속 응원 방지)
		if ("CHEER_FRIEND".equals(event.notificationType()) && event.metadata() != null) {
			Object cheererId = event.metadata().get("cheererId");
			if (cheererId != null) {
				keyBuilder.append(":").append(cheererId);
			}
		}

		// FEED_REACTION의 경우 리액션한 사용자 ID도 추가
		if ("FEED_REACTION".equals(event.notificationType()) && event.metadata() != null) {
			Object reactorId = event.metadata().get("reactorId");
			if (reactorId != null) {
				keyBuilder.append(":").append(reactorId);
			}
		}

		return keyBuilder.toString();
	}

	public void releaseLock(PushNotificationRequestedEvent event) {
		String dedupKey = generateDedupKey(event);
		try {
			redisTemplate.delete(dedupKey);
		} catch (Exception e) {
			log.error("Failed to release dedup lock: key={}", dedupKey, e);
			throw e;
		}
	}

	private Duration getTtlForType(String notificationType) {
		return TTL_BY_TYPE.getOrDefault(notificationType, DEFAULT_TTL);
	}
}
