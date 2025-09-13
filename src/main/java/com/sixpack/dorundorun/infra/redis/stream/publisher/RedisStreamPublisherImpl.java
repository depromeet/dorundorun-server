package com.sixpack.dorundorun.infra.redis.stream.publisher;

import com.sixpack.dorundorun.global.config.redis.stream.RedisStreamProperties;
import com.sixpack.dorundorun.infra.redis.stream.dto.RedisStreamMessage;
import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamPublisherImpl implements RedisStreamPublisher {

	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisStreamProperties properties;
	private final ObjectMapper objectMapper;

	@Override
	public RecordId publish(RedisStreamEvent event) {
		return publish(RedisStreamMessage.of(event));
	}

	@Override
	public void publishAfterCommit(RedisStreamEvent event) {
		publishAfterCommit(RedisStreamMessage.of(event));
	}

	@Override
	public RecordId publish(RedisStreamMessage message) {
		return doPublish(properties.key(), message);
	}

	@Override
	public void publishAfterCommit(RedisStreamMessage message) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			registerTransactionSynchronization(message);
			log.debug("Message scheduled for publication after commit: type={}", message.getType());
		} else {
			log.debug("No active transaction, publishing message immediately: type={}", message.getType());
			publish(message);
		}
	}

	private void registerTransactionSynchronization(RedisStreamMessage message) {
		TransactionSynchronizationManager.registerSynchronization(
				getTransactionSynchronization(message)
		);
	}

	@NotNull
	private TransactionSynchronization getTransactionSynchronization(RedisStreamMessage message) {
		return new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				try {
					publish(message);
					log.debug("Message published after transaction commit: type={}", message.getType());
				} catch (Exception e) {
					log.error("Failed to publish message after commit: type={}", message.getType(), e);
				}
			}

			@Override
			public void afterCompletion(int status) {
				if (status == STATUS_ROLLED_BACK) {
					log.debug("Transaction rolled back, message not published: type={}", message.getType());
				}
			}
		};
	}

	private RecordId doPublish(String streamKey, RedisStreamMessage message) {
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
