package com.sixpack.dorundorun.feature.user.event.handler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.notification.domain.ScheduledNotificationData;
import com.sixpack.dorundorun.feature.user.event.NewUserRunningReminderRequestedEvent;
import com.sixpack.dorundorun.infra.redis.stream.annotation.RedisStreamEventListener;
import com.sixpack.dorundorun.infra.redis.stream.handler.AbstractRedisStreamEventHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RedisStreamEventListener
public class NewUserRunningReminderEventHandler
	extends AbstractRedisStreamEventHandler<NewUserRunningReminderRequestedEvent> {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	public NewUserRunningReminderEventHandler(
		ObjectMapper objectMapper,
		RedisTemplate<String, String> redisTemplate
	) {
		super(objectMapper);
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public String getEventType() {
		return NewUserRunningReminderRequestedEvent.TYPE;
	}

	@Override
	protected Class<NewUserRunningReminderRequestedEvent> payloadType() {
		return NewUserRunningReminderRequestedEvent.class;
	}

	@Override
	protected void onMessage(NewUserRunningReminderRequestedEvent event) throws Exception {
		log.info("Processing new user running reminder event: userId={}", event.userId());

		try {
			// 예약 시간 계산: 지금 + 24시간 (가입 후 24시간)
			LocalDateTime scheduledTime = LocalDateTime.now().plusHours(24);
			long scheduledTimestamp = scheduledTime.atZone(ZoneId.of("Asia/Seoul"))
				.toInstant()
				.getEpochSecond();

			String eventId = UUID.randomUUID().toString();

			ScheduledNotificationData scheduledData = ScheduledNotificationData.builder()
				.eventId(eventId)
				.notificationType("NEW_USER_RUNNING_REMINDER")
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
			redisTemplate.expire("notifications", java.time.Duration.ofDays(4));

			log.info("New user running reminder scheduled: eventId={}, userId={}, scheduledTime={}",
				eventId, event.userId(), scheduledTime);

		} catch (Exception e) {
			log.error("Failed to schedule new user running reminder event: userId={}",
				event.userId(), e);
			throw e;
		}
	}
}
