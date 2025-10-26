package com.sixpack.dorundorun.infra.redis.token;

import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record RefreshToken(
	Long userId,
	String token,
	LocalDateTime expiresAt
) {
	public static RefreshToken of(Long userId, String token, LocalDateTime expiresAt) {
		return RefreshToken.builder()
			.userId(userId)
			.token(token)
			.expiresAt(expiresAt)
			.build();
	}

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiresAt);
	}
}
