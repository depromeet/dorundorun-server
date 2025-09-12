package com.example.team6server.infra.redis.stream.publisher;

import com.example.team6server.infra.redis.stream.dto.RedisStreamMessage;
import com.example.team6server.infra.redis.stream.event.RedisStreamEvent;
import org.springframework.data.redis.connection.stream.RecordId;

public interface RedisStreamPublisher {

	/**
	 * 이벤트를 즉시 발행
	 */
	RecordId publish(RedisStreamEvent event);

	/**
	 * 트랜잭션 커밋 후 이벤트 발행
	 * 트랜잭션이 없는 경우 즉시 발행
	 */
	void publishAfterCommit(RedisStreamEvent event);

	/**
	 * 메시지를 즉시 발행
	 */
	RecordId publish(RedisStreamMessage message);

	/**
	 * 트랜잭션 커밋 후 메시지 발행
	 * 트랜잭션이 없는 경우 즉시 발행
	 */
	void publishAfterCommit(RedisStreamMessage message);
}
