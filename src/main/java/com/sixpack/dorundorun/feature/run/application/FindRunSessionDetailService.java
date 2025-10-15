package com.sixpack.dorundorun.feature.run.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.application.FindFeedByRunSessionIdService;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.run.domain.RunSegments;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.run.domain.RunSessionDetail;
import com.sixpack.dorundorun.feature.run.dto.response.RunSessionDetailResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindRunSessionDetailService {

	private final FindRunSessionByIdAndUserIdService findRunSessionByIdAndUserIdService;
	private final FindAllRunSegmentsByRunSessionIdService findAllRunSegmentsByRunSessionIdService;
	private final FindFeedByRunSessionIdService findFeedByRunSessionIdService;

	@Transactional(readOnly = true)
	public RunSessionDetailResponse find(Long sessionId, Long userId) {
		RunSession runSession = findRunSessionByIdAndUserIdService.find(sessionId, userId);
		RunSegments runSegments = findAllRunSegmentsByRunSessionIdService.find(sessionId);
		Optional<Feed> feed = findFeedByRunSessionIdService.findOrNull(sessionId);

		RunSessionDetail detail = new RunSessionDetail(runSession, runSegments, feed);
		return detail.toResponse();
	}
}
