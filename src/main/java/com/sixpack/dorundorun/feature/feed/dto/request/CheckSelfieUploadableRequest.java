package com.sixpack.dorundorun.feature.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "인증 업로드 가능 여부 확인 요청")
public record CheckSelfieUploadableRequest(
	@Schema(description = "러닝 세션 ID", example = "1", required = true)
	@NotNull(message = "러닝 세션 ID는 필수입니다")
	Long runSessionId
) {
}
