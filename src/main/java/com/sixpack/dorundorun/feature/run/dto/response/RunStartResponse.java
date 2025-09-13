package com.sixpack.dorundorun.feature.run.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record RunStartResponse(
	@Schema(description = "세션 고유 ID", example = "123")
	Long sessionId
) {
}
