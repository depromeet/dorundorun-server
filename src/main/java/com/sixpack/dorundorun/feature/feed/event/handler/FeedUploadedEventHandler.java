package com.sixpack.dorundorun.feature.feed.event.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.feed.application.FindFeedByRunSessionIdService;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.event.FeedUploadedRequestedEvent;
import com.sixpack.dorundorun.feature.friend.dao.FriendJpaRepository;
import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.redis.stream.annotation.RedisStreamEventListener;
import com.sixpack.dorundorun.infra.redis.stream.handler.AbstractRedisStreamEventHandler;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RedisStreamEventListener
public class FeedUploadedEventHandler
	extends AbstractRedisStreamEventHandler<FeedUploadedRequestedEvent> {

	private final FindUserByIdService findUserByIdService;
	private final FindFeedByRunSessionIdService findFeedByRunSessionIdService;
	private final FriendJpaRepository friendJpaRepository;
	private final RedisStreamPublisher redisStreamPublisher;

	public FeedUploadedEventHandler(
		ObjectMapper objectMapper,
		FindUserByIdService findUserByIdService,
		FindFeedByRunSessionIdService findFeedByRunSessionIdService,
		FriendJpaRepository friendJpaRepository,
		RedisStreamPublisher redisStreamPublisher
	) {
		super(objectMapper);
		this.findUserByIdService = findUserByIdService;
		this.findFeedByRunSessionIdService = findFeedByRunSessionIdService;
		this.friendJpaRepository = friendJpaRepository;
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
		log.info("Processing feed uploaded event: userId={}, feedId={}",
			event.userId(), event.feedId());

		try {
			// 피드 조회
			Feed feed = findFeedByRunSessionIdService.findOrNull(event.feedId())
				.orElse(null);

			if (feed == null) {
				log.warn("Feed not found for run session: {}", event.feedId());
				return;
			}

			User uploader = feed.getUser();

			// 게시물 업로드 사용자의 모든 친구 ID 조회
			List<Long> friendIds = friendJpaRepository.findFriendIdsByUserId(uploader.getId());

			if (friendIds == null || friendIds.isEmpty()) {
				log.info("No friends found for user: {}, skipping feed upload notification", uploader.getId());
				return;
			}

			// 메타데이터 생성
			Map<String, Object> metadata = new HashMap<>();
			metadata.put("uploaderName", uploader.getNickname());
			metadata.put("feedId", feed.getId());

			// 각 친구에게 PushNotificationRequestedEvent 발행
			for (Long friendId : friendIds) {
				try {
					// 친구가 실제 사용자인지 확인
					User friend = findUserByIdService.find(friendId);

					PushNotificationRequestedEvent pushEvent = PushNotificationRequestedEvent.builder()
						.recipientUserId(friendId)
						.notificationType("FEED_UPLOADED")
						.relatedId(String.valueOf(feed.getId()))
						.metadata(metadata)
						.build();

					redisStreamPublisher.publishAfterCommit(pushEvent);
					log.debug(
						"Push notification event published for feed upload: recipientId={}, feedId={}, uploaderName={}",
						friendId, feed.getId(), uploader.getNickname());

				} catch (Exception e) {
					log.warn("Failed to send feed upload notification to friend: friendId={}, feedId={}",
						friendId, feed.getId(), e);
				}
			}

			log.info("Feed upload notification completed: uploaderId={}, feedId={}, totalFriends={}",
				uploader.getId(), feed.getId(), friendIds.size());

		} catch (Exception e) {
			log.error("Failed to process feed uploaded event: userId={}, feedId={}",
				event.userId(), event.feedId(), e);
			throw e;
		}
	}
}
