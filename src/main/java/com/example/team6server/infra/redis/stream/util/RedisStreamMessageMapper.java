package com.example.team6server.infra.redis.stream.util;

import com.example.team6server.infra.redis.stream.dto.RedisStreamMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisStreamMessageMapper {

	private final ObjectMapper objectMapper;

	public RedisStreamMessage mapToMessage(String raw) throws Exception {
		if (raw == null || raw.isBlank()) return null;
		String payload = raw;
		if (raw.startsWith("\"") && raw.endsWith("\"")) {
			payload = objectMapper.readValue(raw, String.class);
		}
		return objectMapper.readValue(payload, RedisStreamMessage.class);
	}
}
