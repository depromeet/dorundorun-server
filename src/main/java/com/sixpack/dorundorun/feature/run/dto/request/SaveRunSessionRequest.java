package com.sixpack.dorundorun.feature.run.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "러닝 세부 목표 저장 요청 DTO")
public record SaveRunSessionRequest(
	@Schema(description = "세부 목표 ID", example = "1")
	Long goalPlanId
) {
}
