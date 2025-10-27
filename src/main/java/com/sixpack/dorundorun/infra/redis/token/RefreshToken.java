package com.sixpack.dorundorun.infra.redis.token;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;

@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
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

	@JsonIgnore
	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiresAt);
	}
}
