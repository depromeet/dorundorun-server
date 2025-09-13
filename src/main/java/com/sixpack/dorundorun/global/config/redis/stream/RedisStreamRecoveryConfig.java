package com.sixpack.dorundorun.global.config.redis.stream;

import com.sixpack.dorundorun.infra.redis.stream.recovery.RedisStreamRecovery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
@EnableConfigurationProperties(RedisStreamRecoveryProperties.class)
@ConditionalOnProperty(prefix = "redis.stream.recovery", name = "is-enabled", havingValue = "true")
public class RedisStreamRecoveryConfig {

	private final RedisStreamRecovery recovery;
	private final RedisStreamRecoveryProperties props;

	@Scheduled(fixedDelayString = "${redis.stream.recovery.interval-ms:5000}", initialDelayString = "${redis.stream.recovery.interval-ms:5000}")
	public void runOnceSafely() {
		if (!props.isEnabled()) return;

		long minIdle = props.minIdleMs() > 0 ? props.minIdleMs() : 1_000L;
		long max = props.maxMessages() > 0 ? props.maxMessages() : 50L;

		try {
			recovery.reprocessPending(minIdle, max);
			log.debug("[Recovery] done. minIdleMs={}, maxMessages={}", minIdle, max);
		} catch (Exception e) {
			log.error("[Recovery] failed. minIdleMs={}, maxMessages={}", minIdle, max, e);
		}
	}
}
