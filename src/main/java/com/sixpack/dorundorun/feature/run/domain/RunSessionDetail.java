package com.sixpack.dorundorun.feature.run.domain;

import java.util.Optional;

import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.dto.response.FeedResponse;
import com.sixpack.dorundorun.feature.run.dto.response.RunSessionDetailResponse;

public record RunSessionDetail(
	RunSession runSession,
	RunSegments runSegments,
	Optional<Feed> feed
) {

	public RunSessionDetailResponse toResponse() {
		return new RunSessionDetailResponse(
			runSession.getId(),
			runSession.getCreatedAt(),
			runSession.getUpdatedAt(),
			runSession.getFinishedAt(),
			runSession.getDistanceTotal(),
			runSession.getDurationTotal(),
			runSession.getPaceAvg(),
			runSession.getPaceMax(),
			runSession.getPaceMaxLatitude(),
			runSession.getPaceMaxLongitude(),
			runSession.getCadenceAvg(),
			runSession.getCadenceMax(),
			mapFeedToResponse(),
			runSegments.toSegmentDataList()
		);
	}

	private FeedResponse mapFeedToResponse() {
		return feed.map(FeedResponse::from).orElse(null);
	}
}
