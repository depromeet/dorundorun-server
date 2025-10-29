package com.sixpack.dorundorun.feature.user.event.handler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.notification.domain.ScheduledNotificationData;
import com.sixpack.dorundorun.feature.user.event.NewUserFriendReminderRequestedEvent;
import com.sixpack.dorundorun.infra.redis.stream.annotation.RedisStreamEventListener;
import com.sixpack.dorundorun.infra.redis.stream.handler.AbstractRedisStreamEventHandler;

import lombok.extern.slf4j.Slf4j;

// 신규 사용자 친구추가 독촉 알림 이벤트를 처리하고 Redis Sorted Set에 예약
@Slf4j
@Component
@RedisStreamEventListener
public class NewUserFriendReminderEventHandler
	extends AbstractRedisStreamEventHandler<NewUserFriendReminderRequestedEvent> {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	public NewUserFriendReminderEventHandler(
		ObjectMapper objectMapper,
		RedisTemplate<String, String> redisTemplate
	) {
		super(objectMapper);
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	@Override
	public String getEventType() {
		return NewUserFriendReminderRequestedEvent.TYPE;
	}

	@Override
	protected Class<NewUserFriendReminderRequestedEvent> payloadType() {
		return NewUserFriendReminderRequestedEvent.class;
	}

	@Override
	protected void onMessage(NewUserFriendReminderRequestedEvent event) throws Exception {
		log.info("Processing new user friend reminder event: userId={}", event.userId());

		try {
			// 예약 시간 계산: 지금 + 48시간 (가입 후 48시간)
			LocalDateTime scheduledTime = LocalDateTime.now().plusHours(48);
			long scheduledTimestamp = scheduledTime.atZone(ZoneId.of("Asia/Seoul"))
				.toInstant()
				.getEpochSecond();

			// 고유 이벤트 ID 생성
			String eventId = UUID.randomUUID().toString();

			// ScheduledNotificationData 생성
			ScheduledNotificationData scheduledData = ScheduledNotificationData.builder()
				.eventId(eventId)
				.notificationType("NEW_USER_FRIEND_REMINDER")
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

			log.info("New user friend reminder scheduled: eventId={}, userId={}, scheduledTime={}",
				eventId, event.userId(), scheduledTime);

		} catch (Exception e) {
			log.error("Failed to schedule new user friend reminder event: userId={}",
				event.userId(), e);
			throw e;
		}
	}
}
