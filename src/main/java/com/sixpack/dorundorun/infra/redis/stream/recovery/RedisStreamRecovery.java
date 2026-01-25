package com.sixpack.dorundorun.infra.redis.stream.recovery;

import com.sixpack.dorundorun.global.config.redis.stream.RedisStreamProperties;
import com.sixpack.dorundorun.infra.redis.stream.consumer.RedisStreamMessageProcessor;
import com.sixpack.dorundorun.infra.redis.stream.dto.RedisStreamMessage;
import com.sixpack.dorundorun.infra.redis.stream.util.RedisStreamMessageMapper;

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

	private static final int MAX_RETRY_COUNT = 5;
	private static final long[] BACKOFF_DELAYS_MS = {1000, 2000, 5000, 10000, 30000};

	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisStreamProperties properties;
	private final RedisStreamMessageProcessor processor;
	private final RedisStreamMessageMapper mapper;
	private final DeadLetterQueueService deadLetterQueueService;

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
			long deliveryCount = pm.getTotalDeliveryCount();

			// 최대 재시도 횟수 초과 시 DLQ로 이동
			if (deliveryCount > MAX_RETRY_COUNT) {
				handleMaxRetryExceeded(pm);
				continue;
			}

			// Exponential backoff 적용
			long requiredIdleMs = getBackoffDelay(deliveryCount);
			if (pm.getElapsedTimeSinceLastDelivery().toMillis() < requiredIdleMs) {
				continue;
			}

			List<MapRecord<String, Object, Object>> claimed = redisTemplate.opsForStream().claim(
				properties.key(),
				properties.group(),
				properties.consumerName(),
				Duration.ofMillis(requiredIdleMs),
				pm.getId()
			);

			for (MapRecord<String, Object, Object> record : claimed) {
				try {
					reprocessRecord(record);
				} catch (Exception e) {
					log.error("Reprocess failed for message: id={}, deliveryCount={}", record.getId(), deliveryCount, e);
				}
			}
		}
	}

	private long getBackoffDelay(long deliveryCount) {
		int index = Math.min((int) deliveryCount - 1, BACKOFF_DELAYS_MS.length - 1);
		return index >= 0 ? BACKOFF_DELAYS_MS[index] : BACKOFF_DELAYS_MS[0];
	}

	private void handleMaxRetryExceeded(PendingMessage pm) {
		try {
			// 메시지 내용 조회
			List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().range(
				properties.key(),
				Range.just(pm.getId().getValue())
			);

			String messageJson = "";
			if (records != null && !records.isEmpty()) {
				MapRecord<String, Object, Object> record = records.get(0);
				Object raw = record.getValue().get("value");
				if (raw == null) {
					raw = record.getValue().get("payload");
				}
				messageJson = raw != null ? String.valueOf(raw) : "";
			}

			// DLQ로 이동
			deadLetterQueueService.moveToDeadLetterQueue(
				pm.getId().getValue(),
				messageJson,
				"Max retry count exceeded: " + MAX_RETRY_COUNT,
				pm.getTotalDeliveryCount()
			);

			// ACK 처리하여 PEL에서 제거
			redisTemplate.opsForStream().acknowledge(
				properties.key(),
				properties.group(),
				pm.getId()
			);

			log.warn("Message moved to DLQ after {} retries: id={}", MAX_RETRY_COUNT, pm.getId());

		} catch (Exception e) {
			log.error("Failed to handle max retry exceeded: id={}", pm.getId(), e);
		}
	}

	private void reprocessRecord(MapRecord<String, Object, Object> record) throws Exception {
		Object raw = record.getValue().get("value");
		if (raw == null) {
			raw = record.getValue().get("payload");
		}

		if (raw == null) {
			log.warn("Skip malformed pending message (no value/payload): id={}", record.getId());
			redisTemplate.opsForStream().acknowledge(properties.key(), properties.group(), record.getId());
			return;
		}

		try {
			String json = String.valueOf(raw);
			RedisStreamMessage parsed = mapper.mapToMessage(json);

			if (parsed == null) {
				log.warn("Skip unparsable pending message: id={}", record.getId());
				redisTemplate.opsForStream().acknowledge(properties.key(), properties.group(), record.getId());
				return;
			}

			processor.process(record.getId().getValue(), parsed);

		} catch (Exception e) {
			log.error("Failed to reprocess record: id={}", record.getId(), e);
			throw e;
		}
	}
}