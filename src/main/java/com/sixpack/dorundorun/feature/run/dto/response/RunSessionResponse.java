package com.sixpack.dorundorun.feature.run.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "러닝 세션 DTO")
public record RunSessionResponse(
	@Schema(description = "세션 ID", example = "1")
	Long id,

	@Schema(description = "생성 일시", example = "2024-01-15T09:00:00Z")
	LocalDateTime createdAt,

	@Schema(description = "수정 일시", example = "2024-01-15T10:30:00Z")
	LocalDateTime updatedAt,

	@Schema(description = "세부 목표 ID", example = "1")
	Long goalPlanId,

	@Schema(description = "완료 일시", example = "2024-01-15T10:30:00Z")
	LocalDateTime clearedAt,

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
