package com.example.team6server.global.redis;

import com.example.team6server.infra.redis.stream.event.RedisStreamEvent;

record TestRedisStreamEvent(
		String data
) implements RedisStreamEvent {

	@Override
	public String type() {
		return "TEST_TYPE";
	}
}
