package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.auth.dto.request.RefreshTokenRequest;
import com.sixpack.dorundorun.feature.auth.dto.response.TokenResponse;
import com.sixpack.dorundorun.feature.auth.exception.AuthErrorCode;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.config.jwt.JwtTokenProvider;
import com.sixpack.dorundorun.infra.redis.token.RedisTokenRepository;
import com.sixpack.dorundorun.infra.redis.token.RefreshToken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshAccessTokenService {

	private final JwtTokenProvider jwtTokenProvider;
	private final FindUserByIdService findUserByIdService;
	private final FindRefreshTokenService findRefreshTokenService;
	private final RedisTokenRepository redisTokenRepository;

	@Transactional
	public TokenResponse refresh(RefreshTokenRequest request) {
		String requestToken = request.refreshToken();

		validateRefreshToken(requestToken);
		Long userId = jwtTokenProvider.getUserId(requestToken);
		RefreshToken storedToken = findRefreshTokenService.find(userId);
		validateTokenMatches(requestToken, storedToken);

		redisTokenRepository.delete(userId);

		User user = findUserByIdService.find(userId);
		String newAccessToken = jwtTokenProvider.generateAccessToken(user);
		String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

		redisTokenRepository.save(userId, newRefreshToken);
		log.info("Token refresh completed");

		return TokenResponse.of(newAccessToken, newRefreshToken);
	}

	private void validateRefreshToken(String token) {
		jwtTokenProvider.validate(token);
	}

	private void validateTokenMatches(String requestToken, RefreshToken storedToken) {
		if (!requestToken.equals(storedToken.token())) {
			throw AuthErrorCode.INVALID_TOKEN.format();
		}
	}
}