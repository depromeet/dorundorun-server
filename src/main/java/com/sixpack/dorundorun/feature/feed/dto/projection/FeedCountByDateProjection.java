package com.sixpack.dorundorun.feature.feed.dto.projection;

import java.time.LocalDate;

public interface FeedCountByDateProjection {
	LocalDate getDate();

	Long getCount();
}
