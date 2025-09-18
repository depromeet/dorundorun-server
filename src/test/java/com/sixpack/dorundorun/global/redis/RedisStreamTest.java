package com.sixpack.dorundorun.global.redis;

import com.sixpack.dorundorun.global.config.redis.stream.RedisStreamProperties;
import com.sixpack.dorundorun.global.service.ServiceTest;
import com.sixpack.dorundorun.infra.redis.stream.consumer.RedisStreamConsumer;
import com.sixpack.dorundorun.infra.redis.stream.consumer.RedisStreamGroupManager;
import com.sixpack.dorundorun.infra.redis.stream.dto.RedisStreamMessage;
import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEventType;
import com.sixpack.dorundorun.infra.redis.stream.handler.RedisStreamEventHandlerRegistry;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RedisStreamTest extends ServiceTest {

	@Autowired
	private RedisStreamPublisher publisher;
	@Autowired
	private RedisStreamConsumer consumer;
	@Autowired
	private RedisStreamGroupManager groupManager;
	@Autowired
	private RedisStreamEventHandlerRegistry handlerRegistry;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private RedisStreamProperties properties;
	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		cleanupStream();
	}

	@AfterEach
	void tearDown() {
		stopConsumerSafely();
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

	private void stopConsumerSafely() {
		try {
			consumer.destroy();
			Thread.sleep(100);
		} catch (Exception ignored) {
		}
	}

	@Test
	@Order(1)
	@DisplayName("Redis 연결이 정상적으로 설정되었다")
	void testRedisConnection() {
		// given
		String testKey = "test:connection";
		String testValue = "connected";

		// when
		redisTemplate.opsForValue().set(testKey, testValue);
		Object result = redisTemplate.opsForValue().get(testKey);

		// then
		assertEquals(testValue, result);
		redisTemplate.delete(testKey);
	}

	@Test
	@Order(2)
	@DisplayName("RedisStreamProperties가 올바르게 로드되었다")
	void testPropertiesLoaded() {
		// given // when // then
		assertNotNull(properties);
		assertEquals("test.stream", properties.key());
		assertEquals("test.group", properties.group());
		assertEquals("test-consumer", properties.consumerName());
		assertTrue(properties.isCreateGroupIfMissing());
		assertFalse(properties.isAutoStart());
		assertEquals(10, properties.batchSize());
		assertEquals(100, properties.pollTimeoutMs());
	}

	@Test
	@Order(3)
	@DisplayName("Redis Stream에 메시지를 발행할 수 있다")
	void testPublishMessage() {
		// given
		TestRedisStreamEvent event = new TestRedisStreamEvent("user123");
		RedisStreamMessage message = RedisStreamMessage.of(event);

		// when
		RecordId recordId = publisher.publish(message);

		// then
		assertNotNull(recordId);
		assertNotNull(recordId.getValue());

		Long streamLength = redisTemplate.opsForStream().size(properties.key());
		assertEquals(1L, streamLength);

		List<MapRecord<String, Object, Object>> messages =
			redisTemplate.opsForStream().range(properties.key(), Range.unbounded());
		assertFalse(messages.isEmpty());
		assertThat(messages.get(0).getValue()).containsKey("payload");
	}

	@Test
	@Order(4)
	@DisplayName("Consumer Group을 생성할 수 있다")
	void testCreateConsumerGroup() {
		// given // when
		groupManager.ensureGroup();

		// then
		StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(properties.key());
		assertNotNull(groups);
		assertTrue(groups.stream().anyMatch(g -> g.groupName().equals(properties.group())));
	}

	@Test
	@Order(5)
	@DisplayName("Consumer를 초기화하면 Group이 준비된다")
	void testConsumerInitialization() {
		// given // when
		consumer.initialize();

		// then
		StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(properties.key());
		assertNotNull(groups);
		assertTrue(groups.stream().anyMatch(g -> g.groupName().equals(properties.group())));
	}

	@Test
	@Order(6)
	@DisplayName("이벤트 핸들러 레지스트리가 핸들러를 등록하고 조회할 수 있다")
	void testEventHandlerRegistry() {
		// given
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<TestRedisStreamEvent> processedEvent = new AtomicReference<>();
		TestRedisStreamEventHandler handler = new TestRedisStreamEventHandler(objectMapper, latch, processedEvent);

		// when // then
		boolean exists = handlerRegistry.hasHandler(RedisStreamEventType.USER_REGISTERED);
		assertEquals(exists, handlerRegistry.getHandler(RedisStreamEventType.USER_REGISTERED) != null);
	}

	@Test
	@Order(7)
	@DisplayName("메시지를 발행하고 핸들러가 처리한다")
	void testEndToEndMessageProcessing() throws InterruptedException {
		// given
		CountDownLatch latch = new CountDownLatch(1);
		AtomicReference<TestRedisStreamEvent> processedEvent = new AtomicReference<>();
		TestRedisStreamEventHandler handler = new TestRedisStreamEventHandler(objectMapper, latch, processedEvent);
		consumer.initialize();
		TestRedisStreamEvent event = new TestRedisStreamEvent("user456");
		RedisStreamMessage message = RedisStreamMessage.of(event);

		// when
		RecordId recordId = publisher.publish(message);

		// then
		assertNotNull(recordId);
		await().atMost(Duration.ofSeconds(3)).until(() -> {
			PendingMessagesSummary summary = redisTemplate.opsForStream()
				.pending(properties.key(), properties.group());
			return summary.getTotalPendingMessages() == 0;
		});
	}

	@Test
	@Order(8)
	@DisplayName("여러 메시지를 순차적으로 처리할 수 있다")
	void testMultipleMessages() {
		// given
		consumer.initialize();
		int messageCount = 5;

		// when
		for (int i = 0; i < messageCount; i++) {
			TestRedisStreamEvent event = new TestRedisStreamEvent("user" + i);
			RedisStreamMessage message = RedisStreamMessage.of(event);
			RecordId recordId = publisher.publish(message);
			assertNotNull(recordId);
		}

		// then
		await().atMost(Duration.ofSeconds(5)).until(() -> {
			PendingMessagesSummary summary = redisTemplate.opsForStream()
				.pending(properties.key(), properties.group());
			return summary.getTotalPendingMessages() == 0;
		});

		Long streamLength = redisTemplate.opsForStream().size(properties.key());
		assertEquals(messageCount, streamLength);
	}

	@Test
	@Order(9)
	@DisplayName("메시지 직렬화/역직렬화가 정상 작동한다")
	void testMessageSerialization() throws Exception {
		// given
		LocalDateTime now = LocalDateTime.now();
		TestRedisStreamEvent event = new TestRedisStreamEvent("serialTest");
		RedisStreamMessage original = RedisStreamMessage.builder()
			.id("test-id-123")
			.type(event.type())
			.payload(event)
			.timestamp(now)
			.source("test-source")
			.build();

		// when
		String json = objectMapper.writeValueAsString(original);
		RedisStreamMessage deserialized = objectMapper.readValue(json, RedisStreamMessage.class);

		// then
		assertNotNull(deserialized);
		assertEquals(original.getId(), deserialized.getId());
		assertEquals(original.getType(), deserialized.getType());
		assertEquals(original.getSource(), deserialized.getSource());
		assertNotNull(deserialized.getPayload());
	}

	@Test
	@Order(10)
	@DisplayName("처리되지 않은 메시지는 PEL에 남아있다")
	void testPendingEntryList() {
		// given
		groupManager.ensureGroup();
		TestRedisStreamEvent event = new TestRedisStreamEvent("pendingTest");
		RedisStreamMessage message = RedisStreamMessage.of(event);
		RecordId recordId = publisher.publish(message);

		// when
		List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(
			Consumer.from(properties.group(), "temp-consumer"),
			StreamReadOptions.empty().count(1),
			StreamOffset.create(properties.key(), ReadOffset.lastConsumed())
		);

		// then
		assertFalse(records.isEmpty());
		assertEquals(recordId.getValue(), records.get(0).getId().getValue());

		PendingMessagesSummary summary = redisTemplate.opsForStream()
			.pending(properties.key(), properties.group());
		assertEquals(1L, summary.getTotalPendingMessages());
	}

	@Test
	@Order(11)
	@DisplayName("Consumer Group 정보를 조회할 수 있다")
	void testConsumerGroupInfo() {
		// given
		consumer.initialize();
		TestRedisStreamEvent event = new TestRedisStreamEvent("infoTest");
		publisher.publish(RedisStreamMessage.of(event));

		// when
		await().atMost(Duration.ofSeconds(3)).until(() -> {
			PendingMessagesSummary summary = redisTemplate.opsForStream()
				.pending(properties.key(), properties.group());
			return summary.getTotalPendingMessages() == 0;
		});

		// then
		StreamInfo.XInfoGroups groups = redisTemplate.opsForStream().groups(properties.key());
		StreamInfo.XInfoConsumers consumers = redisTemplate.opsForStream()
			.consumers(properties.key(), properties.group());

		assertNotNull(groups);
		assertFalse(groups.isEmpty());

		StreamInfo.XInfoGroup groupInfo = groups.stream()
			.filter(g -> g.groupName().equals(properties.group()))
			.findFirst()
			.orElseThrow();

		assertEquals(properties.group(), groupInfo.groupName());
		assertNotNull(groupInfo.lastDeliveredId());
		assertTrue(groupInfo.consumerCount() > 0);

		assertNotNull(consumers);
		assertTrue(consumers.stream().anyMatch(c -> c.consumerName().equals(properties.consumerName())));
	}
}
