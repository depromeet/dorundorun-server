package com.sixpack.dorundorun.feature.run.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "러닝 종료 요청 DTO")
public record CompleteRunRequest(
	@Schema(description = "거리 정보")
	DistanceData distance,

	@Schema(description = "시간 정보")
	DurationData duration,

	@Schema(description = "페이스 정보")
	PaceData pace,

	@Schema(description = "케이던스 정보")
	CadenceData cadence
) {
	@Schema(description = "거리 정보")
	public record DistanceData(
		@Schema(description = "총 거리 (m)", example = "5000")
		Long total
	) {}

	@Schema(description = "시간 정보")
	public record DurationData(
		@Schema(description = "총 시간 (초)", example = "1800")
		Long total
	) {}

	@Schema(description = "페이스 정보")
	public record PaceData(
		@Schema(description = "평균 페이스 (초/km)", example = "360")
		Long avg,

		@Schema(description = "최대 페이스 정보")
		MaxPaceData max
	) {}

	@Schema(description = "최대 페이스 정보")
	public record MaxPaceData(
		@Schema(description = "최대 페이스 값 (초/km)", example = "400")
		Long value,

		@Schema(description = "최대 페이스 위도", example = "37.12345")
		Double latitude,

		@Schema(description = "최대 페이스 경도", example = "127.12345")
		Double longitude
	) {}

	@Schema(description = "케이던스 정보")
	public record CadenceData(
		@Schema(description = "평균 케이던스 (걸음/분)", example = "170")
		Integer avg,

		@Schema(description = "최대 케이던스 정보")
		MaxCadenceData max
	) {}

	@Schema(description = "최대 케이던스 정보")
	public record MaxCadenceData(
		@Schema(description = "최대 케이던스 값 (걸음/분)", example = "185")
		Integer value
	) {}
}
