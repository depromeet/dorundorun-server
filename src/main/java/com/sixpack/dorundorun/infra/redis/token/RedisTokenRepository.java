package com.sixpack.dorundorun.infra.redis.token;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.sixpack.dorundorun.global.config.jwt.JwtProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisTokenRepository {

	private static final String KEY_PREFIX = "auth:refresh:";

	private final RedisTemplate<String, RefreshToken> tokenRedisTemplate;
	private final JwtProperties jwtProperties;

	public void save(Long userId, String token) {
		String key = generateKey(userId);
		LocalDateTime expiresAt = LocalDateTime.now()
			.plusSeconds(jwtProperties.refreshTokenValidity() / 1000);

		RefreshToken refreshToken = RefreshToken.of(userId, token, expiresAt);

		tokenRedisTemplate.opsForValue().set(
			key,
			refreshToken,
			jwtProperties.refreshTokenValidity(),
			TimeUnit.MILLISECONDS
		);

		log.debug("Saved refresh token");
	}

	public Optional<RefreshToken> find(Long userId) {
		String key = generateKey(userId);
		RefreshToken refreshToken = tokenRedisTemplate.opsForValue().get(key);

		if (refreshToken == null) {
			log.debug("RefreshToken not found");
		}

		return Optional.ofNullable(refreshToken);
	}

	public void delete(Long userId) {
		String key = generateKey(userId);
		tokenRedisTemplate.delete(key);
		log.debug("Deleted refresh token");
	}

	public boolean exists(Long userId) {
		String key = generateKey(userId);
		return Boolean.TRUE.equals(tokenRedisTemplate.hasKey(key));
	}

	public Duration getRemainingTtl(Long userId) {
		String key = generateKey(userId);
		Long ttl = tokenRedisTemplate.getExpire(key, TimeUnit.MILLISECONDS);

		if (ttl != null && ttl > 0) {
			return Duration.ofMillis(ttl);
		}

		return Duration.ZERO;
	}

	private String generateKey(Long userId) {
		return KEY_PREFIX + userId;
	}
}
