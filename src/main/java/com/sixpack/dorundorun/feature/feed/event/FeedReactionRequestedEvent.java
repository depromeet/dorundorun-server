package com.sixpack.dorundorun.feature.feed.event;

import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;
import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEventType;

import lombok.Builder;

@Builder
public record FeedReactionRequestedEvent(
	Long feedId,
	Long reactorId,
	Long feedOwnerId
) implements RedisStreamEvent {

	public static final String TYPE = RedisStreamEventType.FEED_REACTION_NOTIFICATION_REQUESTED;

	@Override
	public String type() {
		return TYPE;
	}
}
