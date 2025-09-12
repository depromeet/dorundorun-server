package com.example.team6server.infra.redis.stream.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RedisStreamEventHandlerRegistry {

	private final Map<String, RedisStreamEventHandler> handlerMap;

	@Autowired
	public RedisStreamEventHandlerRegistry(List<RedisStreamEventHandler> handlers) {
		this.handlerMap = handlers.stream()
				.collect(Collectors.toMap(
						RedisStreamEventHandler::getEventType,
						Function.identity(),
						(existing, replacement) -> {
							log.warn("Duplicate handler found for event type: {}. Using: {}",
									existing.getEventType(), replacement.getClass().getSimpleName());
							return replacement;
						}
				));
	}

	public RedisStreamEventHandler getHandler(String eventType) {
		return handlerMap.get(eventType);
	}

	public boolean hasHandler(String eventType) {
		return handlerMap.containsKey(eventType);
	}
}
