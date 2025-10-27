package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.auth.exception.AuthErrorCode;
import com.sixpack.dorundorun.infra.redis.token.RedisTokenRepository;
import com.sixpack.dorundorun.infra.redis.token.RefreshToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindRefreshTokenService {

	private final RedisTokenRepository redisTokenRepository;

	public RefreshToken find(Long userId) {
		RefreshToken refreshToken = redisTokenRepository.find(userId)
			.orElseThrow(() -> AuthErrorCode.REFRESH_TOKEN_NOT_FOUND.format());

		if (refreshToken.isExpired()) {
			log.debug("RefreshToken expired");
			redisTokenRepository.delete(userId);
			throw AuthErrorCode.EXPIRED_TOKEN.format();
		}

		return refreshToken;
	}
}