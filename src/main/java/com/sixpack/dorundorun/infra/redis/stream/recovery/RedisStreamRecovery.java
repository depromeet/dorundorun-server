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

	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisStreamProperties properties;
	private final RedisStreamMessageProcessor processor;
	private final RedisStreamMessageMapper mapper;

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
					reprocessRecord(record);
				} catch (Exception e) {
					log.error("Reprocess failed for message: {}", record.getId(), e);
				}
			}
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