package com.sixpack.dorundorun.feature.run.domain;

import java.util.ArrayList;
import java.util.List;

public record RunSegments(
	List<RunSegment> segments
) {

	public List<List<RunSegmentData>> toSegmentDataList() {
		List<List<RunSegmentData>> result = new ArrayList<>();
		List<RunSegmentData> currentSegmentGroup = new ArrayList<>();

		for (RunSegment segment : segments) {
			currentSegmentGroup.addAll(segment.getData().segments());

			if (segment.getData().isStopped()) {
				if (!currentSegmentGroup.isEmpty()) {
					result.add(new ArrayList<>(currentSegmentGroup));
					currentSegmentGroup.clear();
				}
			}
		}

		if (!currentSegmentGroup.isEmpty()) {
			result.add(currentSegmentGroup);
		}

		return result;
	}

	public int size() {
		return segments.size();
	}

	public boolean isEmpty() {
		return segments.isEmpty();
	}
}
