package com.sixpack.dorundorun.global.config.redis.stream;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "redis.stream")
public record RedisStreamProperties(
	String key,
	String group,
	String consumerName,
	boolean isCreateGroupIfMissing,
	boolean isAutoStart,
	int batchSize,
	long pollTimeoutMs
) {
}
