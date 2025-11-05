package com.sixpack.dorundorun.feature.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "디바이스 토큰 수정 요청")
public record DeviceTokenUpdateRequest(
	@NotBlank(message = "디바이스 토큰은 필수입니다")
	@Schema(description = "FCM 디바이스 토큰", example = "fcm_token_example_123")
	String deviceToken
) {
}
