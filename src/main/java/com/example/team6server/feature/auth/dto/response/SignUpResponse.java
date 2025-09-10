package com.example.team6server.feature.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 응답")
public record SignUpResponse(
		@Schema(description = "생성된 사용자 ID", example = "1")
		Long userId,

		@Schema(description = "사용자 이름", example = "홍길동")
		String name,

		@Schema(description = "사용자 이메일", example = "user@example.com")
		String email,

		@Schema(description = "응답 메시지", example = "회원가입이 성공적으로 완료되었습니다.")
		String message
) {
	public static SignUpResponse of(Long userId, String name, String email) {
		return new SignUpResponse(userId, name, email, "회원가입이 성공적으로 완료되었습니다.");
	}
}
