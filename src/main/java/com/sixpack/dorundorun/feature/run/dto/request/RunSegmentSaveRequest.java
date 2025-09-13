package com.sixpack.dorundorun.feature.run.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "러닝 세그먼트 저장 요청 DTO")
public record RunSegmentSaveRequest(
	@Schema(description = "세그먼트 리스트")
	List<RunSegmentRequest> segments,

	@Schema(description = "러닝 중지 여부", example = "false")
	Boolean isStopped
) {
}
