package com.sixpack.dorundorun.feature.friend.event;

import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;
import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEventType;

import lombok.Builder;

@Builder
public record CheerRequestedEvent(
	Long cheererId,
	Long cheeringUserId,
	String idempotencyKey
) implements RedisStreamEvent {

	public static final String TYPE = RedisStreamEventType.CHEER_NOTIFICATION_REQUESTED;

	@Override
	public String type() {
		return TYPE;
	}
}
