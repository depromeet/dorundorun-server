package com.sixpack.dorundorun.feature.run.event.handler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.notification.domain.ScheduledNotificationData;
import com.sixpack.dorundorun.feature.run.event.WeeklyRunningReminderRequestedEvent;
import com.sixpack.dorundorun.infra.redis.stream.annotation.RedisStreamEventListener;
import com.sixpack.dorundorun.infra.redis.stream.handler.AbstractRedisStreamEventHandler;

import lombok.extern.slf4j.Slf4j;

// 러닝 진행 독촉 알림 이벤트를 처리하고 Redis Sorted Set에 예약
@Slf4j
@Component
@RedisStreamEventListener
public class WeeklyRunningReminderEventHandler
	extends AbstractRedisStreamEventHandler<WeeklyRunningReminderRequestedEvent> {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	public WeeklyRunningReminderEventHandler(
		ObjectMapper objectMapper,
		RedisTemplate<String, String> redisTemplate
	) {
		super(objectMapper);
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public String getEventType() {
		return WeeklyRunningReminderRequestedEvent.TYPE;
	}

	@Override
	protected Class<WeeklyRunningReminderRequestedEvent> payloadType() {
		return WeeklyRunningReminderRequestedEvent.class;
	}

	@Override
	protected void onMessage(WeeklyRunningReminderRequestedEvent event) throws Exception {
		log.info("Processing weekly running progress reminder event: userId={}, daysSinceLastRun={}",
			event.userId(), event.daysSinceLastRun());

		try {
			// 예약 시간 계산: 지금 + 7일 (마지막 러닝 후 7일)
			LocalDateTime scheduledTime = LocalDateTime.now().plusDays(7);
			long scheduledTimestamp = scheduledTime.atZone(ZoneId.of("Asia/Seoul"))
				.toInstant()
				.getEpochSecond();

			// 고유 이벤트 ID 생성
			String eventId = UUID.randomUUID().toString();

			// ScheduledNotificationData 생성
			ScheduledNotificationData scheduledData = ScheduledNotificationData.builder()
				.eventId(eventId)
				.notificationType("RUNNING_PROGRESS_REMINDER")
				.userId(event.userId())
				.scheduledAt(scheduledTime)
				.build();

			// Redis Sorted Set에 추가
			redisTemplate.opsForZSet().add(
				"pending-notifications",
				eventId,
				scheduledTimestamp
			);

			// Redis Hash에 이벤트 데이터 저장
			String eventJson = objectMapper.writeValueAsString(scheduledData);
			redisTemplate.opsForHash().put(
				"notifications",
				eventId,
				eventJson
			);

			// TTL 설정
			redisTemplate.expire("notifications", java.time.Duration.ofDays(30));

			log.info(
				"Weekly running progress reminder scheduled: eventId={}, userId={}, daysSinceLastRun={}, scheduledTime={}",
				eventId, event.userId(), event.daysSinceLastRun(), scheduledTime);

		} catch (Exception e) {
			log.error("Failed to schedule weekly running progress reminder event: userId={}",
				event.userId(), e);
			throw e;
		}
	}
}
