package com.sixpack.dorundorun.feature.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "인증 업로드 가능 여부 확인 응답")
public record CheckSelfieUploadableResponse(
	@Schema(description = "인증 업로드 가능 여부", example = "true")
	boolean isUploadable,

	@Schema(description = "인증 불가능 사유 (업로드 가능하면 null)", example = "ALREADY_UPLOADED_TODAY", allowableValues = {
		"ALREADY_UPLOADED_TODAY", "RUN_NOT_TODAY"})
	String reason
) {
	public static CheckSelfieUploadableResponse uploadable() {
		return new CheckSelfieUploadableResponse(true, null);
	}

	public static CheckSelfieUploadableResponse notUploadable(String reason) {
		return new CheckSelfieUploadableResponse(false, reason);
	}
}
