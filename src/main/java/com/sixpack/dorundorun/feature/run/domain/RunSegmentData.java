package com.sixpack.dorundorun.feature.run.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record RunSegmentData(
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
		LocalDateTime time,
		Double latitude,
		Double longitude,
		Double altitude,
		Long distance,
		Long pace,
		Long speed,
		Integer cadence
) {
}
