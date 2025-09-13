package com.sixpack.dorundorun.feature.goal.dto.response;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "최신 목표 조회 응답")
public record LatestGoalResponse(
	@Schema(description = "목표 고유 ID", example = "1")
	Long id,

	@Schema(description = "생성 일시 (ISO 8601)", example = "2024-01-01T09:00:00Z")
	Instant createdAt,

	@Schema(description = "수정 일시 (ISO 8601)", example = "2024-01-01T09:00:00Z")
	Instant updatedAt,

	@Schema(description = "목표 제목", example = "하프마라톤 완주")
	String title,

	@Schema(description = "목표 시작일 (YYYY-MM-DD)", example = "2024-01-01")
	String startedAt,

	@Schema(description = "목표 종료일 (YYYY-MM-DD)", example = "2024-06-30")
	String endedAt,

	@Schema(description = "목표 달성 일시 (null 가능)", example = "null")
	Instant clearedAt,

	@Schema(description = "목표 페이스 (초/km)", example = "360")
	Integer pace,

	@Schema(description = "목표 거리 (m)", example = "21097")
	Integer distance,

	@Schema(description = "목표 시간 (초)", example = "7200")
	Integer duration,

	@Schema(description = "반복 유형", example = "week")
	String repeatType,

	@Schema(description = "반복 빈도", example = "3")
	Integer repeatFrequency
) {
}
