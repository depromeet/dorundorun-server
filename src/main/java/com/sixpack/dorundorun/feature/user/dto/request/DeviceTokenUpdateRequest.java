package com.sixpack.dorundorun.feature.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "디바이스 토큰 수정 요청")
public record DeviceTokenUpdateRequest(
	@Schema(description = "FCM 디바이스 토큰", example = "fcm_token_example_123")
	String deviceToken
) {
}
