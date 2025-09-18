package com.sixpack.dorundorun.infra.redis.stream.consumer;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.sixpack.dorundorun.global.config.redis.stream.RedisStreamProperties;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.output.StatusOutput;
import io.lettuce.core.protocol.CommandArgs;
import io.lettuce.core.protocol.CommandKeyword;
import io.lettuce.core.protocol.CommandType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamGroupManager {

	private final RedisTemplate<String, Object> redisTemplate;
	private final RedisStreamProperties properties;

	public void ensureGroup() {
		String streamKey = properties.key();
		String groupName = properties.group();

		try {
			Boolean exists = redisTemplate.hasKey(streamKey);
			if (Boolean.FALSE.equals(exists)) {
				createStreamWithMkStream(streamKey, groupName);
				return;
			}
			if (!isGroupExists(streamKey, groupName)) {
				redisTemplate.opsForStream().createGroup(streamKey, ReadOffset.from("0"), groupName);
				log.info("Consumer group created: {}", groupName);
			}
		} catch (Exception e) {
			log.error("ensureGroup failed", e);
			throw new IllegalStateException("Failed to ensure stream/group", e);
		}
	}

	private boolean isGroupExists(String streamKey, String groupName) {
		try {
			return redisTemplate.opsForStream()
				.groups(streamKey)
				.stream()
				.anyMatch(g ->
					groupName.equals(g.groupName())
				);
		} catch (Exception e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private void createStreamWithMkStream(String streamKey, String groupName) {
		try (RedisConnection connection = redisTemplate.getConnectionFactory().getConnection()) {
			Object nativeConn = connection.getNativeConnection();
			if (nativeConn instanceof RedisAsyncCommands<?, ?> commands) {
				RedisAsyncCommands<String, String> async = (RedisAsyncCommands<String, String>)commands;
				CommandArgs<String, String> args = new CommandArgs<>(StringCodec.UTF8)
					.add(CommandKeyword.CREATE).add(streamKey).add(groupName).add("0").add("MKSTREAM");
				RedisFuture<String> future = async.dispatch(CommandType.XGROUP, new StatusOutput<>(StringCodec.UTF8),
					args);
				future.get(5, TimeUnit.SECONDS);
				log.info("Stream+Group created with MKSTREAM: key={}, group={}", streamKey, groupName);
			}
		} catch (Exception e) {
			log.error("MKSTREAM failed", e);
			throw new IllegalStateException("MKSTREAM failed", e);
		}
	}
}
