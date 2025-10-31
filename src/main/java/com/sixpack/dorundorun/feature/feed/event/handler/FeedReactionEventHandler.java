package com.sixpack.dorundorun.feature.feed.event.handler;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.feed.event.FeedReactionRequestedEvent;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.redis.stream.annotation.RedisStreamEventListener;
import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;
import com.sixpack.dorundorun.infra.redis.stream.handler.AbstractRedisStreamEventHandler;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RedisStreamEventListener
public class FeedReactionEventHandler
	extends AbstractRedisStreamEventHandler<FeedReactionRequestedEvent> {

	private final FindUserByIdService findUserByIdService;
	private final RedisStreamPublisher redisStreamPublisher;

	public FeedReactionEventHandler(
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
		return FeedReactionRequestedEvent.TYPE;
	}

	@Override
	protected Class<FeedReactionRequestedEvent> payloadType() {
		return FeedReactionRequestedEvent.class;
	}

	@Override
	protected void onMessage(FeedReactionRequestedEvent event) throws Exception {
		log.info("Processing feed reaction event: feedId={}, reactorId={}, feedOwnerId={}",
			event.feedId(), event.reactorId(), event.feedOwnerId());

		try {
			User reactor = findUserByIdService.find(event.reactorId());

			// 메타데이터 생성
			Map<String, Object> metadata = new HashMap<>();
			metadata.put("reactorName", reactor.getNickname());
			metadata.put("feedId", event.feedId());

			// PushNotificationRequestedEvent로 변환하여 발행
			PushNotificationRequestedEvent pushEvent = PushNotificationRequestedEvent.builder()
				.recipientUserId(event.feedOwnerId())
				.notificationType("FEED_REACTION")
				.relatedId(String.valueOf(event.feedId()))
				.metadata(metadata)
				.build();

			redisStreamPublisher.publishAfterCommit(pushEvent);
			log.info("Push notification event published for feed reaction: recipientId={}, feedId={}, reactorId={}",
				event.feedOwnerId(), event.feedId(), event.reactorId());

		} catch (Exception e) {
			log.error("Failed to process feed reaction event: feedId={}, reactorId={}, feedOwnerId={}",
				event.feedId(), event.reactorId(), event.feedOwnerId(), e);
			throw e;
		}
	}
}
