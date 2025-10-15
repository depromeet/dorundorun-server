package com.sixpack.dorundorun.feature.feed.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "주차별 셀피 목록 조회 요청")
public class SelfieWeekListRequest {

	@Schema(description = "조회 시작 날짜", example = "2025-09-20", required = true)
	@NotNull(message = "시작 날짜는 필수입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;

	@Schema(description = "조회 종료 날짜", example = "2025-09-26", required = true)
	@NotNull(message = "종료 날짜는 필수입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;

	public SelfieWeekListRequest(LocalDate startDate, LocalDate endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
	}
}
