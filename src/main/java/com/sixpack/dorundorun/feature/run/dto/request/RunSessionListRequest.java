package com.sixpack.dorundorun.feature.run.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "러닝 세션 목록 조회 요청 필터 DTO")
public record RunSessionListRequest(
	@Schema(description = "피드 인증 여부 필터", example = "false", nullable = true)
	Boolean isSelfied,

	@Schema(description = "조회 시작 시간 (이 시간 이후 생성된 세션)", example = "2024-01-15T09:00:00.000000Z", nullable = true)
	LocalDateTime startDateTime,

	@Schema(description = "조회 끝 시간", example = "2024-01-17T09:00:00.000000Z", nullable = true)
	LocalDateTime endDateTime
) {
}
