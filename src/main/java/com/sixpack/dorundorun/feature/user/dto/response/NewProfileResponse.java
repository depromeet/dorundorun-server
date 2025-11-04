package com.sixpack.dorundorun.feature.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "새 프로필 이미지 url")
public record NewProfileResponse(
	@Schema(description = "프로필 이미지 URL (presigned URL)", example = "https://.../profile/abc.jpg", nullable = true)
	String profileImageUrl
) {
}
