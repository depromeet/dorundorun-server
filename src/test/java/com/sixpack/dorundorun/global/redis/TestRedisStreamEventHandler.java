package com.sixpack.dorundorun.global.redis;

import com.sixpack.dorundorun.infra.redis.stream.handler.AbstractRedisStreamEventHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

class TestRedisStreamEventHandler extends AbstractRedisStreamEventHandler<TestRedisStreamEvent> {

	private final CountDownLatch latch;
	private final AtomicReference<TestRedisStreamEvent> processedEvent;

	public TestRedisStreamEventHandler(ObjectMapper objectMapper, CountDownLatch latch,
		AtomicReference<TestRedisStreamEvent> processedEvent) {
		super(objectMapper);
		this.latch = latch;
		this.processedEvent = processedEvent;
	}

	@Override
	public String getEventType() {
		return "TEST_TYPE";
	}

	@Override
	protected Class<TestRedisStreamEvent> payloadType() {
		return TestRedisStreamEvent.class;
	}

	@Override
	protected void onMessage(TestRedisStreamEvent event) throws Exception {
		processedEvent.set(event);
		latch.countDown();
	}
}
