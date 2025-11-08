package com.sixpack.dorundorun.feature.run.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "러닝 세션 DTO")
public record RunSessionResponse(
	@Schema(description = "세션 ID", example = "1")
	Long id,

	@Schema(description = "생성 일시", example = "2024-01-15T09:00:00.000000Z")
	LocalDateTime createdAt,

	@Schema(description = "수정 일시", example = "2024-01-15T10:30:00.000000Z")
	LocalDateTime updatedAt,

	@Schema(description = "완료 일시", example = "2024-01-15T10:30:00.000000Z")
	LocalDateTime finishedAt,

	@Schema(description = "총 거리 (m)", example = "5000")
	Long distanceTotal,

	@Schema(description = "총 시간 (초)", example = "1800")
	Long durationTotal,

	@Schema(description = "평균 페이스 (초/km)", example = "360")
	Long paceAvg,

	@Schema(description = "최대 페이스 (초/km)", example = "360")
	Long paceMax,

	@Schema(description = "최대 페이스 위도", example = "37.5301")
	Double paceMaxLatitude,

	@Schema(description = "최대 페이스 경도", example = "127.12345")
	Double paceMaxLongitude,

	@Schema(description = "평균 케이던스 (걸음/분)", example = "170")
	Integer cadenceAvg,

	@Schema(description = "최대 케이던스 (걸음/분)", example = "185")
	Integer cadenceMax,

	@Schema(description = "맵 이미지 URL", example = "https://example.com/map.jpg")
	String mapImage
) {
}
