package com.sixpack.dorundorun.feature.feed.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.Parameter;

public record FeedListRequest(
	@Parameter(description = "조회할 날짜 (null이면 모든 날짜)")
	LocalDate currentDate,

	@Parameter(description = "조회할 사용자 ID (null이면 전체 피드, not null이면 사용자 ID에 맞는 피드)")
	Long userId,

	@Parameter(description = "페이지 번호", example = "0")
	Integer page,

	@Parameter(description = "페이지 크기", example = "20")
	Integer size
) {
	public FeedListRequest {
		if (page == null)
			page = 0;
		if (size == null)
			size = 20;
	}
}