package com.sixpack.dorundorun.feature.feed.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "특정 날짜 셀피 유저 목록 조회 요청")
public record SelfieUsersRequest(
	@Schema(description = "조회할 날짜", example = "2025-10-16", required = true)
	@NotNull(message = "날짜는 필수입니다.")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	LocalDate date
) {
}
