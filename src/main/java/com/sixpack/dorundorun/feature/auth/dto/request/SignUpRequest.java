package com.sixpack.dorundorun.feature.auth.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 요청")
public record SignUpRequest(
	@Schema(description = "전화번호 (SMS 인증 완료된 번호)", example = "010-1234-5678")
	String phoneNumber,

	@Schema(description = "닉네임", example = "러너123")
	String nickname,

	@Schema(description = "약관 동의 정보")
	ConsentInfo consent,

	@Schema(description = "디바이스 토큰", example = "fcm_token_example_123")
	String deviceToken
) {
	@Schema(description = "약관 동의 정보")
	public record ConsentInfo(
		@Schema(description = "마케팅 수신 동의 시각", example = "2025-10-16T14:30:00")
		LocalDateTime marketingConsentAt,

		@Schema(description = "위치 정보 수집 동의 시각", example = "2025-10-16T14:30:00")
		LocalDateTime locationConsentAt,

		@Schema(description = "개인정보 수집 동의 시각 (필수)", example = "2025-10-16T14:30:00")
		LocalDateTime personalConsentAt
	) {
	}
}
