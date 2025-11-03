package com.sixpack.dorundorun.feature.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 프로필 수정 요청")
public record MeProfileUpdateRequest(
	@Schema(description = "닉네임 (2~8자, 한글 허용)", example = "두런두런")
	String nickname,

	@Schema(description = "프로필 이미지 삭제 여부 (true: 삭제, false: 유지 또는 새 이미지로 교체)", example = "false")
	Boolean removeProfileImage
) {
}
