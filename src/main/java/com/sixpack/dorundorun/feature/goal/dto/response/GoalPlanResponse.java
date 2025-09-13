package com.sixpack.dorundorun.feature.goal.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "세부 목표 단건 응답 DTO")
public record GoalPlanResponse(
	@Schema(description = "세부 목표 ID", example = "1")
	Long id,

	@Schema(description = "생성 일시", example = "2024-01-01T09:00:00Z")
	Instant createdAt,

	@Schema(description = "수정 일시", example = "2024-01-01T09:00:00Z")
	Instant updatedAt,

	@Schema(description = "달성 일시", example = "2024-01-01T09:00:00Z")
	Instant clearedAt,

	@Schema(description = "최종 목표 ID", example = "1")
	Long goalId,

	@Schema(description = "페이스 (초/km)", example = "360")
	Integer pace,

	@Schema(description = "거리 (m)", example = "21097")
	Integer distance,

	@Schema(description = "시간 (초)", example = "7200")
	Integer duration,

	@Schema(description = "회차 번호", example = "7")
	Integer roundCount,

	@Schema(description = "전체 회차 수", example = "12")
	Integer totalRoundCount
) {
}
