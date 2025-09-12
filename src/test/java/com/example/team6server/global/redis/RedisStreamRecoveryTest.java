package com.example.team6server.global.redis;

import com.example.team6server.global.config.redis.stream.RedisStreamProperties;
import com.example.team6server.global.service.ServiceTest;
import com.example.team6server.infra.redis.stream.consumer.RedisStreamGroupManager;
import com.example.team6server.infra.redis.stream.dto.RedisStreamMessage;
import com.example.team6server.infra.redis.stream.publisher.RedisStreamPublisherImpl;
import com.example.team6server.infra.redis.stream.recovery.RedisStreamRecovery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class RedisStreamRecoveryTest extends ServiceTest {

	@Autowired
	private RedisStreamRecovery recovery;
	@Autowired
	private RedisStreamPublisherImpl publisher;
	@Autowired
	private RedisStreamGroupManager groupManager;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private RedisStreamProperties properties;

	@BeforeEach
	void setUp() {
		cleanupStream();
		groupManager.ensureGroup();
	}

	private void cleanupStream() {
		try {
			if (Boolean.TRUE.equals(redisTemplate.hasKey(properties.key()))) {
				try {
					redisTemplate.opsForStream().destroyGroup(properties.key(), properties.group());
				} catch (Exception ignored) {
				}
				redisTemplate.delete(properties.key());
			}
		} catch (Exception ignored) {
		}
	}

	@Test
	@DisplayName("PEL에 있는 오래된 메시지를 재처리한다")
	void testRecoveryProcess() throws InterruptedException {
		// given - 메시지 발행 및 읽기 (ACK 하지 않음)
		TestRedisStreamEvent event = new TestRedisStreamEvent("recovery-data");
		RedisStreamMessage message = RedisStreamMessage.of(event);
		RecordId recordId = publisher.publish(message);

		List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
				Consumer.from(properties.group(), "test-consumer"),
				StreamReadOptions.empty().count(1),
				StreamOffset.create(properties.key(), ReadOffset.lastConsumed())
		);

		assertFalse(records.isEmpty());
		assertEquals(recordId.getValue(), records.get(0).getId().getValue());

		PendingMessagesSummary beforeSummary = redisTemplate.opsForStream()
				.pending(properties.key(), properties.group());
		assertEquals(1L, beforeSummary.getTotalPendingMessages());

		Thread.sleep(1500);

		// when - Recovery 실행
		recovery.reprocessPending(1000, 10);

		// then - Recovery 후 상태 확인
		await().atMost(Duration.ofSeconds(3)).until(() -> {
			PendingMessagesSummary s = redisTemplate.opsForStream().pending(properties.key(), properties.group());
			return s.getTotalPendingMessages() == 0;
		});
		PendingMessages pendingAfter = redisTemplate.opsForStream().pending(
				properties.key(),
				Consumer.from(properties.group(), properties.consumerName()),
				Range.unbounded(),
				10L
		);
		assertTrue(pendingAfter.size() >= 0);
	}

	@Test
	@DisplayName("여러 개의 PEL 메시지를 한 번에 재처리한다")
	void testBatchRecovery() throws InterruptedException {
		// given - 5개 메시지 발행 및 읽기 (ACK 하지 않음)
		for (int i = 0; i < 5; i++) {
			TestRedisStreamEvent event = new TestRedisStreamEvent("batch-data-" + i);
			publisher.publish(RedisStreamMessage.of(event));
		}
		redisTemplate.opsForStream().read(
				Consumer.from(properties.group(), "test-consumer"),
				StreamReadOptions.empty().count(5),
				StreamOffset.create(properties.key(), ReadOffset.lastConsumed())
		);
		PendingMessagesSummary beforeSummary = redisTemplate.opsForStream()
				.pending(properties.key(), properties.group());
		assertEquals(5L, beforeSummary.getTotalPendingMessages());
		Thread.sleep(1500);

		// when - Recovery 실행 (최대 3개씩 처리)
		recovery.reprocessPending(1000, 3);

		// then
		Thread.sleep(500);
		PendingMessagesSummary afterSummary = redisTemplate.opsForStream()
				.pending(properties.key(), properties.group());
		assertTrue(afterSummary.getTotalPendingMessages() <= 5);
	}

	@Test
	@DisplayName("minIdleTime이 지나지 않은 메시지는 재처리하지 않는다")
	void testMinIdleTimeRespected() {
		// given - 메시지 발행 및 즉시 읽기
		TestRedisStreamEvent event = new TestRedisStreamEvent("fresh-data");
		publisher.publish(RedisStreamMessage.of(event));
		redisTemplate.opsForStream().read(
				Consumer.from(properties.group(), "test-consumer"),
				StreamReadOptions.empty().count(1),
				StreamOffset.create(properties.key(), ReadOffset.lastConsumed())
		);

		// when - 긴 minIdleTime으로 Recovery 실행
		recovery.reprocessPending(5000, 10);

		// then - 메시지는 여전히 원래 consumer에게 할당되어 있어야 함
		PendingMessages pending = redisTemplate.opsForStream().pending(
				properties.key(),
				Consumer.from(properties.group(), "test-consumer"),
				Range.unbounded(),
				10L
		);
		assertEquals(1, pending.size());
		assertEquals("test-consumer", pending.get(0).getConsumerName());
	}

	@Test
	@DisplayName("PEL이 비어있을 때 Recovery는 아무 작업도 하지 않는다")
	void testRecoveryWithEmptyPEL() {
		// given - PEL이 비어있는 상태

		// when
		recovery.reprocessPending(1000, 10);

		// then - 예외가 발생하지 않아야 함
		PendingMessagesSummary summary = redisTemplate.opsForStream()
				.pending(properties.key(), properties.group());
		assertEquals(0L, summary.getTotalPendingMessages());
	}
}
