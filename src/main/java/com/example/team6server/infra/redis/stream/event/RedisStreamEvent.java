package com.example.team6server.infra.redis.stream.event;

public interface RedisStreamEvent {
	String type();

	default String id() {
		return "";
	}
}
