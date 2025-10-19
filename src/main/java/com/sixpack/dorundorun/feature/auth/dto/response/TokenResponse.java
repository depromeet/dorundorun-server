package com.sixpack.dorundorun.feature.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 갱신 응답")
public record TokenResponse(
	@Schema(description = "Access Token")
	String accessToken,

	@Schema(description = "Refresh Token")
	String refreshToken
) {
	public static TokenResponse of(String accessToken, String refreshToken) {
		return new TokenResponse(accessToken, refreshToken);
	}
}
