package com.sixpack.dorundorun.feature.run.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "수기 러닝 기록 응답 DTO")
public record ManualRunSessionCompleteResponse(
	@Schema(description = "러닝 종료 시간")
	LocalDateTime finishedAt,

	@Schema(description = "총 시간 (초)")
	Long durationTotal,

	@Schema(description = "총 거리 (m)")
	Long distanceTotal,

	@Schema(description = "평균 페이스 (초/km)")
	Double paceAvg,

	@Schema(description = "평균 케이던스 (걸음/분)")
	Integer cadenceAvg
) {
}