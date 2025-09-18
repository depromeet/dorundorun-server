package com.sixpack.dorundorun.global.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaginationInfo {
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean first;
	private boolean last;
	private boolean hasNext;
	private boolean hasPrevious;
}
