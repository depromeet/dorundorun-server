package com.sixpack.dorundorun.infra.redis.stream.event;

public interface RedisStreamEvent {

	String type();

	default String id() {
		return "";
	}
}
