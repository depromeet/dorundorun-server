package com.example.team6server.infra.redis.stream.handler;

import com.example.team6server.infra.redis.stream.dto.RedisStreamMessage;

public interface RedisStreamEventHandler {

	/**
	 * 처리할 수 있는 이벤트 타입을 반환
	 */
	String getEventType();

	/**
	 * 비즈니스 로직 처리
	 */
	void handle(RedisStreamMessage message);
}