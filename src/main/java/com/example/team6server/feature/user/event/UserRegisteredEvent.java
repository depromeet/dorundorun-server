package com.example.team6server.feature.user.event;

import com.example.team6server.infra.redis.stream.event.RedisStreamEvent;
import com.example.team6server.infra.redis.stream.event.RedisStreamEventType;
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
