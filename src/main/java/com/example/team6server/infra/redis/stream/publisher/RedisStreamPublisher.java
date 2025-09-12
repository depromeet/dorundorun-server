package com.example.team6server.infra.redis.stream.publisher;

import com.example.team6server.global.config.redis.stream.RedisStreamProperties;
import com.example.team6server.infra.redis.stream.dto.RedisStreamMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamPublisher {

	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisStreamProperties properties;
	private final ObjectMapper objectMapper;

	public RecordId publish(RedisStreamMessage message) {
		return publish(properties.key(), message);
	}

	private RecordId publish(String streamKey, RedisStreamMessage message) {
		try {
			String jsonMessage = objectMapper.writeValueAsString(message);

			ObjectRecord<String, String> record = StreamRecords.newRecord()
					.ofObject(jsonMessage)
					.withStreamKey(streamKey);

			RecordId recordId = redisTemplate.opsForStream().add(record);

			if (recordId == null) {
				throw new RuntimeException("Failed to publish message to Redis Stream");
			}

			log.info("Published message: StreamKey={}, RecordId={}, Type={}",
					streamKey, recordId.getValue(), message.getType());

			return recordId;

		} catch (Exception e) {
			log.error("Error publishing message to Redis Stream", e);
			throw new RuntimeException("Error publishing message to Redis Stream", e);
		}
	}
}
