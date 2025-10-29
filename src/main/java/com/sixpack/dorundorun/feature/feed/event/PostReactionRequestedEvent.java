package com.sixpack.dorundorun.feature.feed.event;

import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;
import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEventType;

import lombok.Builder;

@Builder
public record PostReactionRequestedEvent(
	Long postId,
	Long reactorId,
	Long postOwnerId
) implements RedisStreamEvent {

	public static final String TYPE = RedisStreamEventType.POST_REACTION_NOTIFICATION_REQUESTED;

	@Override
	public String type() {
		return TYPE;
	}
}
