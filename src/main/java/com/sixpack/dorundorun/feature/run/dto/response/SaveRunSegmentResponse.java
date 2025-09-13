package com.sixpack.dorundorun.feature.run.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "러닝 세그먼트 저장 응답 DTO")
public record SaveRunSegmentResponse(
	@Schema(description = "세그먼트 ID", example = "123")
	Long segmentId,

	@Schema(description = "저장된 데이터 포인트 수", example = "2")
	Integer savedCount
) {
}
