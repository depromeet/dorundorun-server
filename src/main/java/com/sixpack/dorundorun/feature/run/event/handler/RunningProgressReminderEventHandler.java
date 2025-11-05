package com.sixpack.dorundorun.feature.run.event.handler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.notification.domain.ScheduledNotificationData;
import com.sixpack.dorundorun.feature.run.event.RunningProgressReminderRequestedEvent;
import com.sixpack.dorundorun.infra.redis.stream.annotation.RedisStreamEventListener;
import com.sixpack.dorundorun.infra.redis.stream.handler.AbstractRedisStreamEventHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RedisStreamEventListener
public class RunningProgressReminderEventHandler
	extends AbstractRedisStreamEventHandler<RunningProgressReminderRequestedEvent> {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	public RunningProgressReminderEventHandler(
		ObjectMapper objectMapper,
		RedisTemplate<String, String> redisTemplate
	) {
		super(objectMapper);
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public String getEventType() {
		return RunningProgressReminderRequestedEvent.TYPE;
	}

	@Override
	protected Class<RunningProgressReminderRequestedEvent> payloadType() {
		return RunningProgressReminderRequestedEvent.class;
	}

	@Override
	protected void onMessage(RunningProgressReminderRequestedEvent event) throws Exception {
		log.info("Processing running progress reminder event: userId={}, runSessionId={}",
			event.userId(), event.runSessionId());

		try {
			// 예약 시간 계산: 지금 + 7일 (마지막 러닝 완료 후 7일)
			LocalDateTime scheduledTime = LocalDateTime.now().plusDays(7);
			long scheduledTimestamp = scheduledTime.atZone(ZoneId.of("Asia/Seoul"))
				.toInstant()
				.getEpochSecond();

			String eventId = UUID.randomUUID().toString();

			java.util.Map<String, Object> additionalData = new java.util.HashMap<>();
			additionalData.put("runSessionId", event.runSessionId());

			ScheduledNotificationData scheduledData = ScheduledNotificationData.builder()
				.eventId(eventId)
				.notificationType("RUNNING_PROGRESS_REMINDER")
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
			// value: 직렬화된 ScheduledNotificationData
			String eventJson = objectMapper.writeValueAsString(scheduledData);
			redisTemplate.opsForHash().put(
				"notifications",
				eventId,
				eventJson
			);

			// TTL 설정 (30일 후 자동 삭제)
			redisTemplate.expire("notifications", java.time.Duration.ofDays(8));

			log.info("Running progress reminder scheduled: eventId={}, userId={}, runSessionId={}, scheduledTime={}",
				eventId, event.userId(), event.runSessionId(), scheduledTime);

		} catch (Exception e) {
			log.error("Failed to schedule running progress reminder event: userId={}, runSessionId={}",
				event.userId(), event.runSessionId(), e);
			throw e;
		}
	}
}
