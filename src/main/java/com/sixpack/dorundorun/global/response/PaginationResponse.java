package com.sixpack.dorundorun.global.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PaginationResponse<T> {

	private List<T> contents;
	private PaginationInfo meta;

	public static <T> PaginationResponse<T> of(List<T> contents, int page, int size, long totalElements) {
		int totalPages = (int)Math.ceil((double)totalElements / size);

		PaginationInfo paginationInfo = PaginationInfo.builder()
			.page(page)
			.size(size)
			.totalElements(totalElements)
			.totalPages(totalPages)
			.first(page == 0)
			.last(page == totalPages - 1)
			.hasNext(page < totalPages - 1)
			.hasPrevious(page > 0)
			.build();

		return PaginationResponse.<T>builder()
			.contents(contents)
			.meta(paginationInfo)
			.build();
	}
}
