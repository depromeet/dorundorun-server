package com.sixpack.dorundorun.feature.feed.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "주차별 셀피 인증 수 응답")
public class SelfieWeekResponse {

	@Schema(description = "날짜별 인증 수 목록")
	private final List<DailyCertification> data;

	public SelfieWeekResponse(List<DailyCertification> data) {
		this.data = data;
	}

	@Getter
	@Schema(description = "일별 인증 정보")
	public static class DailyCertification {
		@Schema(description = "날짜 (YYYY-MM-DD)", example = "2025-10-06")
		private final String date;

		@Schema(description = "인증 개수", example = "12")
		private final Integer selfieCount;

		public DailyCertification(String date, Integer selfieCount) {
			this.date = date;
			this.selfieCount = selfieCount;
		}
	}
}
