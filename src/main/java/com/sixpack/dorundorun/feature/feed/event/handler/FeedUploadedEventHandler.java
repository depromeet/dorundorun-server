package com.sixpack.dorundorun.feature.feed.event.handler;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.feed.application.FindFeedByRunSessionIdService;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.event.FeedUploadedRequestedEvent;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.redis.stream.annotation.RedisStreamEventListener;
import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;
import com.sixpack.dorundorun.infra.redis.stream.handler.AbstractRedisStreamEventHandler;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RedisStreamEventListener
public class FeedUploadedEventHandler
	extends AbstractRedisStreamEventHandler<FeedUploadedRequestedEvent> {

	private final FindUserByIdService findUserByIdService;
	private final FindFeedByRunSessionIdService findFeedByRunSessionIdService;
	private final RedisStreamPublisher redisStreamPublisher;

	public FeedUploadedEventHandler(
		ObjectMapper objectMapper,
		FindUserByIdService findUserByIdService,
		FindFeedByRunSessionIdService findFeedByRunSessionIdService,
		RedisStreamPublisher redisStreamPublisher
	) {
		super(objectMapper);
		this.findUserByIdService = findUserByIdService;
		this.findFeedByRunSessionIdService = findFeedByRunSessionIdService;
		this.redisStreamPublisher = redisStreamPublisher;
	}

	@Override
	public String getEventType() {
		return FeedUploadedRequestedEvent.TYPE;
	}

	@Override
	protected Class<FeedUploadedRequestedEvent> payloadType() {
		return FeedUploadedRequestedEvent.class;
	}

	@Override
	protected void onMessage(FeedUploadedRequestedEvent event) throws Exception {
		log.info("Processing feed uploaded event: userId={}, certificationId={}",
			event.userId(), event.certificationId());

		try {
			// 피드 조회
			Feed feed = findFeedByRunSessionIdService.findOrNull(event.certificationId())
				.orElse(null);

			if (feed == null) {
				log.warn("Feed not found for run session: {}", event.certificationId());
				return;
			}

			User uploader = feed.getUser();

			// 메타데이터 생성
			Map<String, Object> metadata = new HashMap<>();
			metadata.put("uploaderName", uploader.getNickname());
			metadata.put("feedId", feed.getId());

			// PushNotificationRequestedEvent로 변환하여 발행
			PushNotificationRequestedEvent pushEvent = PushNotificationRequestedEvent.builder()
				.recipientUserId(event.userId())
				.notificationType("CERTIFICATION_UPLOADED")
				.relatedId(String.valueOf(feed.getId()))
				.metadata(metadata)
				.build();

			redisStreamPublisher.publishAfterCommit(pushEvent);
			log.info("Push notification event published for feed upload: recipientId={}, feedId={}",
				event.userId(), feed.getId());

		} catch (Exception e) {
			log.error("Failed to process feed uploaded event: userId={}, certificationId={}",
				event.userId(), event.certificationId(), e);
			throw e;
		}
	}
}
