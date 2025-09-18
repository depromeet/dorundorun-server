package com.sixpack.dorundorun.global.config.redis.stream;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "redis.stream.recovery")
public record RedisStreamRecoveryProperties(
	boolean isEnabled,
	long intervalMs,
	long minIdleMs,
	long maxMessages
) {
}
