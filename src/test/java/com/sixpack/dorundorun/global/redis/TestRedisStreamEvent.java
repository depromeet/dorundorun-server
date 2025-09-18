package com.sixpack.dorundorun.global.redis;

import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;

record TestRedisStreamEvent(
	String data
) implements RedisStreamEvent {

	@Override
	public String type() {
		return "TEST_TYPE";
	}
}
