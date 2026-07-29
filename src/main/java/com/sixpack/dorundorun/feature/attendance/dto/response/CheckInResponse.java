package com.sixpack.dorundorun.feature.attendance.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "출석 체크인 응답 DTO")
public record CheckInResponse(
	@Schema(description = "현재 연속 방문 일수", example = "5")
	int streakCount,

	@Schema(description = "역대 최장 연속 방문 일수", example = "12")
	int longestStreak,

	@Schema(description = "오늘 첫 체크인 여부", example = "true")
	boolean isFirstCheckInToday
) {
}
