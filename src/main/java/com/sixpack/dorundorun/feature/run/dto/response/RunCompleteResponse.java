package com.sixpack.dorundorun.feature.run.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "러닝 종료 응답 DTO")
public record RunCompleteResponse(
	@Schema(description = "세션 ID", example = "1")
	Long id,

	@Schema(description = "생성 일시", example = "2024-01-15T09:00:00Z")
	Instant createdAt,

	@Schema(description = "수정 일시", example = "2024-01-15T10:30:00Z")
	Instant updatedAt,

	@Schema(description = "최종 목표 ID", example = "1")
	Long finalGoalId,

	@Schema(description = "완료 일시", example = "2024-01-15T10:30:00Z")
	Instant clearedAt,

	@Schema(description = "회차 번호", example = "5")
	Integer roundCount,

	@Schema(description = "총 거리 (m)", example = "5000")
	Integer totalDistance,

	@Schema(description = "총 시간 (초)", example = "1800")
	Integer totalDuration,

	@Schema(description = "평균 페이스 (초/km)", example = "360")
	Integer avgPace,

	@Schema(description = "평균 케이던스", example = "170")
	Integer avgCadence,

	@Schema(description = "최대 케이던스", example = "185")
	Integer maxCadence
) {
}
