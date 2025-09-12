package com.example.team6server.infra.redis.stream.consumer;

import com.example.team6server.global.config.redis.stream.RedisStreamProperties;
import com.example.team6server.infra.redis.stream.dto.RedisStreamMessage;
import com.example.team6server.infra.redis.stream.util.RedisStreamMessageMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamConsumer implements StreamListener<String, ObjectRecord<String, String>> {

	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisStreamProperties properties;
	private final RedisStreamGroupManager groupManager;
	private final RedisStreamMessageMapper mapper;
	private final RedisStreamMessageProcessor processor;

	private final AtomicBoolean initialized = new AtomicBoolean(false);
	private volatile StreamMessageListenerContainer<String, ObjectRecord<String, String>> container;
	private volatile Subscription subscription;

	@PostConstruct
	public void autoStart() {
		if (properties.isAutoStart()) initialize();
	}

	public void initialize() {
		if (!initialized.compareAndSet(false, true)) return;

		groupManager.ensureGroup();

		container = StreamMessageListenerContainer.create(
				redisTemplate.getConnectionFactory(),
				StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
						.pollTimeout(Duration.ofMillis(properties.pollTimeoutMs()))
						.targetType(String.class)
						.build()
		);

		subscription = container.receive(
				Consumer.from(properties.group(), properties.consumerName()),
				StreamOffset.create(properties.key(), ReadOffset.lastConsumed()),
				this
		);

		container.start();
		log.info("Redis Stream Consumer started: key={}, group={}, consumer={}",
				properties.key(), properties.group(), properties.consumerName());
	}

	@Override
	public void onMessage(ObjectRecord<String, String> message) {
		String recordId = message.getId().getValue();
		try {
			RedisStreamMessage parsed = mapper.mapToMessage(message.getValue());
			if (parsed == null) {
				log.warn("Skip empty message: id={}", recordId);
				return;
			}
			processor.process(recordId, parsed);
		} catch (Exception e) {
			log.error("onMessage error: id={}", recordId, e);
			// PEL 유지 → Recovery가 처리 / 필요시 재시도 로직 추가하기
		}
	}

	@PreDestroy
	public void destroy() {
		if (!initialized.compareAndSet(true, false)) return;
		try {
			if (subscription != null) subscription.cancel();
			if (container != null && container.isRunning()) container.stop();
			log.info("Redis Stream Consumer stopped");
		} catch (Exception e) {
			log.error("Shutdown error", e);
		}
	}
}
