package com.sixpack.dorundorun.feature.run.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "러닝 응답 DTO")
public record SaveRunSessionResponse(
	@Schema(description = "세션 고유 ID", example = "123")
	Long sessionId
) {
}
