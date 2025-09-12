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
		if (raw == null || raw.isEmpty()) return null;
		if (raw.startsWith("\"") && raw.endsWith("\"")) {
			raw = raw.substring(1, raw.length() - 1).replace("\\\"", "\"");
		}
		return objectMapper.readValue(raw, RedisStreamMessage.class);
	}
}
