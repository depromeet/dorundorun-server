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

	private final RedisTemplate<String, Object> redisTemplate;
	private final JwtProperties jwtProperties;

	public void save(Long userId, String token) {
		String key = generateKey(userId);
		LocalDateTime expiresAt = LocalDateTime.now()
			.plusSeconds(jwtProperties.refreshTokenValidity() / 1000);

		RefreshToken refreshToken = RefreshToken.of(userId, token, expiresAt);

		redisTemplate.opsForValue().set(
			key,
			refreshToken,
			jwtProperties.refreshTokenValidity(),
			TimeUnit.MILLISECONDS
		);

		log.debug("Saved refresh token for userId: {}", userId);
	}

	public Optional<RefreshToken> find(Long userId) {
		String key = generateKey(userId);
		Object value = redisTemplate.opsForValue().get(key);

		if (value instanceof RefreshToken refreshToken) {
			if (refreshToken.isExpired()) {
				delete(userId);
				return Optional.empty();
			}
			return Optional.of(refreshToken);
		}

		return Optional.empty();
	}

	public void delete(Long userId) {
		String key = generateKey(userId);
		redisTemplate.delete(key);
		log.debug("Deleted refresh token for userId: {}", userId);
	}

	public boolean exists(Long userId) {
		String key = generateKey(userId);
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	public Duration getRemainingTtl(Long userId) {
		String key = generateKey(userId);
		Long ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);

		if (ttl != null && ttl > 0) {
			return Duration.ofMillis(ttl);
		}

		return Duration.ZERO;
	}

	private String generateKey(Long userId) {
		return KEY_PREFIX + userId;
	}
}
