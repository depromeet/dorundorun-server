package com.sixpack.dorundorun.feature.friend.event.handler;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.friend.event.CheerRequestedEvent;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.redis.stream.annotation.RedisStreamEventListener;
import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;
import com.sixpack.dorundorun.infra.redis.stream.handler.AbstractRedisStreamEventHandler;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RedisStreamEventListener
public class CheerEventHandler extends AbstractRedisStreamEventHandler<CheerRequestedEvent> {

	private final FindUserByIdService findUserByIdService;
	private final RedisStreamPublisher redisStreamPublisher;

	public CheerEventHandler(
		ObjectMapper objectMapper,
		FindUserByIdService findUserByIdService,
		RedisStreamPublisher redisStreamPublisher
	) {
		super(objectMapper);
		this.findUserByIdService = findUserByIdService;
		this.redisStreamPublisher = redisStreamPublisher;
	}

	@Override
	public String getEventType() {
		return CheerRequestedEvent.TYPE;
	}

	@Override
	protected Class<CheerRequestedEvent> payloadType() {
		return CheerRequestedEvent.class;
	}

	@Override
	protected void onMessage(CheerRequestedEvent event) throws Exception {
		log.info("Processing cheer event: cheererId={}, cheeringUserId={}",
			event.cheererId(), event.cheeringUserId());

		try {
			User cheerer = findUserByIdService.find(event.cheererId());

			// 메타데이터 생성
			Map<String, Object> metadata = new HashMap<>();
			metadata.put("senderId", event.cheererId());
			metadata.put("cheererName", cheerer.getNickname());

			// PushNotificationRequestedEvent로 변환하여 발행
			PushNotificationRequestedEvent pushEvent = PushNotificationRequestedEvent.builder()
				.recipientUserId(event.cheeringUserId())
				.notificationType("CHEER_FRIEND")
				.relatedId(String.valueOf(event.cheererId()))
				.metadata(metadata)
				.build();

			redisStreamPublisher.publishAfterCommit(pushEvent);
			log.info("Push notification event published for cheer: recipientId={}, cheererId={}",
				event.cheeringUserId(), event.cheererId());

		} catch (Exception e) {
			log.error("Failed to process cheer event: cheererId={}, cheeringUserId={}",
				event.cheererId(), event.cheeringUserId(), e);
			throw e;
		}
	}
}
