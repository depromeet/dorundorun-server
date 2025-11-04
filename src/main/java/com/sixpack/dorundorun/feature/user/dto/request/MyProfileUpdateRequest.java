package com.sixpack.dorundorun.feature.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 프로필 수정 요청")
public record MyProfileUpdateRequest(
	@Schema(description = "닉네임 (2~8자)", example = "두런두런")
	String nickname,

	@Schema(
		description = """
			프로필 이미지 처리 옵션:
			- SET: 새 이미지로 교체 (profileImage 파일 필수)
			- REMOVE: 이미지 삭제
			- KEEP: 기존 이미지 유지 (또는 null일 경우 기본값)
			""",
		example = "KEEP",
		allowableValues = {"SET", "REMOVE", "KEEP"}
	)
	ProfileImageOption imageOption
) {
}
