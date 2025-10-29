package com.sixpack.dorundorun.feature.feed.event.handler;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.feed.event.PostReactionRequestedEvent;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.redis.stream.annotation.RedisStreamEventListener;
import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;
import com.sixpack.dorundorun.infra.redis.stream.handler.AbstractRedisStreamEventHandler;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RedisStreamEventListener
public class PostReactionEventHandler
	extends AbstractRedisStreamEventHandler<PostReactionRequestedEvent> {

	private final FindUserByIdService findUserByIdService;
	private final RedisStreamPublisher redisStreamPublisher;

	public PostReactionEventHandler(
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
		return PostReactionRequestedEvent.TYPE;
	}

	@Override
	protected Class<PostReactionRequestedEvent> payloadType() {
		return PostReactionRequestedEvent.class;
	}

	@Override
	protected void onMessage(PostReactionRequestedEvent event) throws Exception {
		log.info("Processing post reaction event: postId={}, reactorId={}, postOwnerId={}",
			event.postId(), event.reactorId(), event.postOwnerId());

		try {
			User reactor = findUserByIdService.find(event.reactorId());

			// 메타데이터 생성
			Map<String, Object> metadata = new HashMap<>();
			metadata.put("reactorName", reactor.getNickname());
			metadata.put("postId", event.postId());

			// PushNotificationRequestedEvent로 변환하여 발행
			PushNotificationRequestedEvent pushEvent = PushNotificationRequestedEvent.builder()
				.recipientUserId(event.postOwnerId())
				.notificationType("POST_REACTION")
				.relatedId(String.valueOf(event.postId()))
				.metadata(metadata)
				.build();

			redisStreamPublisher.publishAfterCommit(pushEvent);
			log.info("Push notification event published for post reaction: recipientId={}, postId={}, reactorId={}",
				event.postOwnerId(), event.postId(), event.reactorId());

		} catch (Exception e) {
			log.error("Failed to process post reaction event: postId={}, reactorId={}, postOwnerId={}",
				event.postId(), event.reactorId(), event.postOwnerId(), e);
			throw e;
		}
	}
}
