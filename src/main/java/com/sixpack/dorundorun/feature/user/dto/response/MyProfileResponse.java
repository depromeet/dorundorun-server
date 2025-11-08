package com.sixpack.dorundorun.feature.user.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 프로필 상세 응답")
public record MyProfileResponse(
	@Schema(description = "사용자 ID", example = "1")
	Long id,

	@Schema(description = "닉네임", example = "두런두런두런")
	String nickname,

	@Schema(description = "프로필 이미지 URL (presigned URL)", example = "https://.../profile/abc.jpg", nullable = true)
	String profileImageUrl,

	@Schema(description = "사용자 코드 (초대/식별 코드)", example = "ABC123")
	String code,

	@Schema(description = "전화번호", example = "010-7724-8020")
	String phoneNumberFormatted,

	@Schema(description = "가입 시간", example = "2024-01-15T10:30:00.000000Z")
	LocalDateTime createdAt
) {
}
