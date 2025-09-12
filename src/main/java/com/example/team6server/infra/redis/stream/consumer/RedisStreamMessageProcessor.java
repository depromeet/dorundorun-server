package com.example.team6server.infra.redis.stream.consumer;

import com.example.team6server.global.config.redis.stream.RedisStreamProperties;
import com.example.team6server.infra.redis.stream.dto.RedisStreamMessage;
import com.example.team6server.infra.redis.stream.handler.RedisStreamEventHandler;
import com.example.team6server.infra.redis.stream.handler.RedisStreamEventHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamMessageProcessor {

	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisStreamEventHandlerRegistry handlerRegistry;
	private final RedisStreamProperties properties;

	public void process(String recordId, RedisStreamMessage message) {
		String type = message.getType();

		if (!handlerRegistry.hasHandler(type)) {
			log.warn("No handler for type={}", type);
			this.ack(recordId);
			return;
		}

		RedisStreamEventHandler handler = handlerRegistry.getHandler(type);
		try {
			log.debug("Dispatch to handler: type={}, handler={}", type, handler.getClass().getSimpleName());
			handler.handle(message);
			this.ack(recordId);
		} catch (Exception e) {
			log.error("Handler failed: type={}, id={}", type, recordId, e);
			// ACK 하지 않음 → PEL 유지 → Recovery 가 재처리
			throw e;
		}
	}

	private void ack(String recordId) {
		redisTemplate.opsForStream().acknowledge(properties.key(), properties.group(), recordId);
	}
}
