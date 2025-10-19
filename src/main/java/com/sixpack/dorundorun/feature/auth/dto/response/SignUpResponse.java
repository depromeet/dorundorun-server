package com.sixpack.dorundorun.feature.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답")
public record SignUpResponse(
	@Schema(description = "사용자 정보")
	UserInfo user,

	@Schema(description = "토큰 정보")
	TokenInfo token
) {
	@Schema(description = "사용자 정보")
	public record UserInfo(
		@Schema(description = "사용자 ID", example = "1")
		Long id,

		@Schema(description = "닉네임", example = "러너123")
		String nickname,

		@Schema(description = "전화번호", example = "010-1234-5678")
		String phoneNumber
	) {}

	@Schema(description = "토큰 정보")
	public record TokenInfo(
		@Schema(description = "Access Token")
		String accessToken,

		@Schema(description = "Refresh Token")
		String refreshToken
	) {}

	public static SignUpResponse of(
		Long userId,
		String nickname,
		String phoneNumber,
		String accessToken,
		String refreshToken
	) {
		return new SignUpResponse(
			new UserInfo(userId, nickname, phoneNumber),
			new TokenInfo(accessToken, refreshToken)
		);
	}
}
