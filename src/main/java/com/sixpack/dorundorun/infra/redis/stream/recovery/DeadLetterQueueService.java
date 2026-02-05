package com.sixpack.dorundorun.infra.redis.stream.recovery;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.global.config.redis.stream.RedisStreamProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadLetterQueueService {

	private static final String DLQ_KEY_SUFFIX = ":dlq";

	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisStreamProperties properties;
	private final ObjectMapper objectMapper;

	/**
	 * 실패한 메시지를 DLQ로 이동
	 * @return true면 DLQ 이동 성공, false면 실패
	 */
	public boolean moveToDeadLetterQueue(
		String recordId,
		String messageJson,
		String errorMessage,
		long retryCount
	) {
		try {
			String dlqKey = properties.key() + DLQ_KEY_SUFFIX;

			Map<String, Object> dlqEntry = new HashMap<>();
			dlqEntry.put("originalRecordId", recordId);
			dlqEntry.put("originalMessage", messageJson);
			dlqEntry.put("errorMessage", errorMessage);
			dlqEntry.put("retryCount", retryCount);
			dlqEntry.put("movedAt", LocalDateTime.now().toString());
			dlqEntry.put("originalStream", properties.key());

			String dlqEntryJson = objectMapper.writeValueAsString(dlqEntry);

			redisTemplate.opsForStream().add(
				StreamRecords.newRecord()
					.ofObject(dlqEntryJson)
					.withStreamKey(dlqKey)
			);

			log.warn("Message moved to DLQ: recordId={}, retryCount={}, error={}",
				recordId, retryCount, errorMessage);

			return true;
		} catch (Exception e) {
			log.error("Failed to move message to DLQ: recordId={}", recordId, e);
			return false;
		}
	}

	/**
	 * DLQ에 있는 메시지 수 반환
	 */
	public long getDlqSize() {
		String dlqKey = properties.key() + DLQ_KEY_SUFFIX;
		Long size = redisTemplate.opsForStream().size(dlqKey);
		return size != null ? size : 0;
	}
}
