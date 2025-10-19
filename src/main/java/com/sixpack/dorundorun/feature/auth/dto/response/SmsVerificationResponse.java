package com.sixpack.dorundorun.feature.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SMS 인증 확인 응답")
public record SmsVerificationResponse(
	@Schema(description = "전화번호", example = "010-1234-5678")
	String phoneNumber,

	@Schema(description = "기존 회원 여부", example = "true")
	Boolean isExistingUser,

	@Schema(description = "사용자 정보 (기존 회원일 경우)")
	UserInfo user,

	@Schema(description = "토큰 정보 (기존 회원일 경우)")
	TokenInfo token
) {
	@Schema(description = "사용자 정보")
	public record UserInfo(
		@Schema(description = "사용자 ID", example = "1")
		Long id,

		@Schema(description = "닉네임", example = "러너123")
		String nickname
	) {}

	@Schema(description = "토큰 정보")
	public record TokenInfo(
		@Schema(description = "Access Token")
		String accessToken,

		@Schema(description = "Refresh Token")
		String refreshToken
	) {}

	// 기존 회원
	public static SmsVerificationResponse ofExistingUser(
		String phoneNumber,
		Long userId,
		String nickname,
		String accessToken,
		String refreshToken
	) {
		return new SmsVerificationResponse(
			phoneNumber,
			true,
			new UserInfo(userId, nickname),
			new TokenInfo(accessToken, refreshToken)
		);
	}

	// 신규 회원
	public static SmsVerificationResponse ofNewUser(String phoneNumber) {
		return new SmsVerificationResponse(phoneNumber, false, null, null);
	}
}
