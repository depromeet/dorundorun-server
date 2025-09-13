package com.sixpack.dorundorun.infra.redis.stream.handler;

import com.sixpack.dorundorun.infra.redis.stream.dto.RedisStreamMessage;
import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractRedisStreamEventHandler<T extends RedisStreamEvent>
		implements RedisStreamEventHandler {

	private final ObjectMapper objectMapper;

	/**
	 * 라우팅 키
	 */
	@Override
	public abstract String getEventType();

	/**
	 * 변환 대상 타입
	 */
	protected abstract Class<T> payloadType();

	/**
	 * 비즈니스 로직
	 */
	protected abstract void onMessage(T event) throws Exception;

	@Override
	public void handle(RedisStreamMessage message) {
		try {
			T event = message.getPayloadAs(objectMapper, payloadType());
			this.onMessage(event);
		} catch (Exception e) {
			log.error("Event handling failed. type={}, id={}", message.getType(), message.getId(), e);
			throw new RuntimeException("Event handling failed", e);
		}
	}
}
