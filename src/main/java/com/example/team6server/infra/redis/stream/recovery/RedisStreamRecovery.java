package com.example.team6server.infra.redis.stream.recovery;

import com.example.team6server.global.config.redis.stream.RedisStreamProperties;
import com.example.team6server.infra.redis.stream.dto.RedisStreamMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamRecovery {

	private final RedisTemplate<String, String> redisTemplate;
	private final RedisStreamProperties properties;
	private final ObjectMapper objectMapper;

	public void reprocessPending(long minIdleMs, long count) {
		PendingMessagesSummary summary = redisTemplate.opsForStream()
				.pending(properties.key(), properties.group());

		if (summary == null || summary.getTotalPendingMessages() == 0) {
			return;
		}

		log.info("Pending messages: total={}, minId={}, maxId={}",
				summary.getTotalPendingMessages(),
				summary.minMessageId(),
				summary.maxMessageId());

		PendingMessages pending = redisTemplate.opsForStream().pending(
				properties.key(),
				properties.group(),
				Range.unbounded(),
				count
		);

		for (PendingMessage pm : pending) {
			if (pm.getElapsedTimeSinceLastDelivery().toMillis() < minIdleMs) {
				continue;
			}

			List<MapRecord<String, Object, Object>> claimed = redisTemplate.opsForStream().claim(
					properties.key(),
					properties.group(),
					properties.consumerName(),
					Duration.ofMillis(minIdleMs),
					pm.getId()
			);

			for (MapRecord<String, Object, Object> record : claimed) {
				try {
					processRecord(record);
					redisTemplate.opsForStream().acknowledge(
							properties.key(),
							properties.group(),
							record.getId()
					);
				} catch (Exception e) {
					log.error("Reprocess failed for message: {}", record.getId(), e);
				}
			}
		}
	}

	private void processRecord(MapRecord<String, Object, Object> record) throws Exception {
		Object payload = record.getValue().get("payload");
		if (payload == null) return;

		String jsonString = payload.toString();
		if (jsonString.startsWith("\"") && jsonString.endsWith("\"")) {
			jsonString = jsonString.substring(1, jsonString.length() - 1).replace("\\\"", "\"");
		}

		RedisStreamMessage message = objectMapper.readValue(jsonString, RedisStreamMessage.class);
		log.info("Reprocessing message: Type={}, RecordId={}", message.getType(), record.getId());
	}
}
