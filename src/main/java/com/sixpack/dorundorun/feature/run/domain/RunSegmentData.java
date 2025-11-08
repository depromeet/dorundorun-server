package com.sixpack.dorundorun.feature.run.domain;

import java.time.LocalDateTime;

public record RunSegmentData(
	LocalDateTime time,
	Double latitude,
	Double longitude,
	Double altitude,
	Long distance,
	Long pace,
	Double speed,
	Integer cadence
) {
}
