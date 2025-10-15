package com.sixpack.dorundorun.feature.run.domain;

import java.util.List;

public record RunSegmentInfo(
	List<RunSegmentData> segments,
	boolean isStopped
) {
}
