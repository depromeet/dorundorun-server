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
							throw new IllegalStateException(String.format(
									"Duplicate handler for event type '%s': %s vs %s",
									existing.getEventType(),
									existing.getClass().getName(),
									replacement.getClass().getName()));
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
