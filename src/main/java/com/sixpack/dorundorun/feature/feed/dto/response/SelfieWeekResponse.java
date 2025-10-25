package com.sixpack.dorundorun.feature.feed.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주차별 셀피 인증 수 응답")
public record SelfieWeekResponse(

	@Schema(description = "날짜별 인증 수 목록")
	List<DailyCertification> data
) {
	@Schema(description = "일별 인증 정보")
	public record DailyCertification(
		@Schema(description = "날짜 (YYYY-MM-DD)", example = "2025-10-06")
		String date,

		@Schema(description = "인증 개수", example = "12")
		Integer selfieCount
	) {
	}
}
