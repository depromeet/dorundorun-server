package com.sixpack.dorundorun.feature.run.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "러닝 종료 요청 DTO")
public record RunCompleteRequest(
	@Schema(description = "총 거리 (m)", example = "5000")
	Long totalDistance,

	@Schema(description = "총 시간 (초)", example = "1800")
	Long totalDuration,

	@Schema(description = "평균 페이스 (초/km)", example = "360")
	Long avgPace,

	@Schema(description = "평균 케이던스", example = "170")
	Integer avgCadence,

	@Schema(description = "최대 케이던스", example = "185")
	Integer maxCadence
) {
}
