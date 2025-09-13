package com.sixpack.dorundorun.feature.user.event;

import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;
import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEventType;
import lombok.Builder;

@Builder
public record UserRegisteredEvent(
		Long userId,
		String email,
		String name
) implements RedisStreamEvent {

	public static final String TYPE = RedisStreamEventType.USER_REGISTERED;

	@Override
	public String type() {
		return TYPE;
	}
}
