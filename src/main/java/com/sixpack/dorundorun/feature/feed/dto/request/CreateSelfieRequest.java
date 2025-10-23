package com.sixpack.dorundorun.feature.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "인증피드 등록 요청")
public record CreateSelfieRequest(
	@Schema(description = "러닝 세션 ID", example = "123")
	@NotNull(message = "러닝 세션 ID는 필수입니다.")
	Long runSessionId,

	@Schema(description = "피드 내용", example = "오늘도 완주! 🏃‍♂️")
	String content
) {
}
