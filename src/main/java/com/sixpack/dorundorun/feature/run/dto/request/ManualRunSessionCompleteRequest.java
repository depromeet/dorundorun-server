package com.sixpack.dorundorun.feature.run.dto.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "수기 러닝 기록 요청 DTO")
public record ManualRunSessionCompleteRequest(
	@Schema(description = "러닝 시작 일시")
	@NotNull
	LocalDateTime startedAt,

	@Schema(description = "총 시간 (초)")
	@NotNull
	Long durationTotal,

	@Schema(description = "총 거리 (m)")
	@NotNull
	Long distanceTotal,

	@Schema(description = "평균 페이스 (초/km)")
	@NotNull
	Double paceAvg,

	@Schema(description = "평균 케이던스 (걸음/분)")
	@NotNull
	Integer cadenceAvg
) {
}
