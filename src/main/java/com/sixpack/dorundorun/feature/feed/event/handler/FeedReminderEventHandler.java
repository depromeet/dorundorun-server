package com.sixpack.dorundorun.feature.feed.event.handler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.feed.event.FeedReminderRequestedEvent;
import com.sixpack.dorundorun.feature.notification.domain.ScheduledNotificationData;
import com.sixpack.dorundorun.infra.redis.stream.annotation.RedisStreamEventListener;
import com.sixpack.dorundorun.infra.redis.stream.handler.AbstractRedisStreamEventHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RedisStreamEventListener
public class FeedReminderEventHandler
	extends AbstractRedisStreamEventHandler<FeedReminderRequestedEvent> {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	public FeedReminderEventHandler(
		ObjectMapper objectMapper,
		RedisTemplate<String, String> redisTemplate
	) {
		super(objectMapper);
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public String getEventType() {
		return FeedReminderRequestedEvent.TYPE;
	}

	@Override
	protected Class<FeedReminderRequestedEvent> payloadType() {
		return FeedReminderRequestedEvent.class;
	}

	@Override
	protected void onMessage(FeedReminderRequestedEvent event) throws Exception {
		log.info("Processing feed reminder event: userId={}, runSessionId={}",
			event.userId(), event.runSessionId());

		try {
			// 예약 시간 계산: 지금 + 23시간 (러닝 완료 후 23시간)
			LocalDateTime scheduledTime = LocalDateTime.now().plusHours(23);
			long scheduledTimestamp = scheduledTime.atZone(ZoneId.of("Asia/Seoul"))
				.toInstant()
				.getEpochSecond();

			String eventId = UUID.randomUUID().toString();

			// ScheduledNotificationData 생성
			// additionalData에 runSessionId 저장 (23시간 후 게시물 여부 확인용)
			java.util.Map<String, Object> additionalData = new java.util.HashMap<>();
			additionalData.put("runSessionId", event.runSessionId());

			ScheduledNotificationData scheduledData = ScheduledNotificationData.builder()
				.eventId(eventId)
				.notificationType("FEED_REMINDER")
				.userId(event.userId())
				.scheduledAt(scheduledTime)
				.additionalData(additionalData)
				.build();

			// Redis Sorted Set에 추가
			// key: "pending-notifications"
			// score: 예약 시간의 Unix timestamp
			// member: eventId
			redisTemplate.opsForZSet().add(
				"pending-notifications",
				eventId,
				scheduledTimestamp
			);

			// Redis Hash에 이벤트 데이터 저장
			// key: "notifications"
			// field: eventId
			// value: ScheduledNotificationData
			String eventJson = objectMapper.writeValueAsString(scheduledData);
			redisTemplate.opsForHash().put(
				"notifications",
				eventId,
				eventJson
			);

			// TTL 설정 (1일 후 자동 삭제)
			redisTemplate.expire("notifications", java.time.Duration.ofDays(1));

			log.info("Feed reminder scheduled: eventId={}, userId={}, runSessionId={}, scheduledTime={}",
				eventId, event.userId(), event.runSessionId(), scheduledTime);

		} catch (Exception e) {
			log.error("Failed to schedule feed reminder event: userId={}, runSessionId={}",
				event.userId(), event.runSessionId(), e);
			throw e;
		}
	}
}
