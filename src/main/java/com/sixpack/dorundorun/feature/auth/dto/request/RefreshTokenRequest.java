package com.sixpack.dorundorun.feature.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토큰 갱신 요청")
public record RefreshTokenRequest(
	@Schema(description = "Refresh Token", example = "eyJhbGci...")
	String refreshToken
) {}
