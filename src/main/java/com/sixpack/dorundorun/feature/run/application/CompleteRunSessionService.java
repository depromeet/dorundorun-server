package com.sixpack.dorundorun.feature.run.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.run.dto.request.CompleteRunRequest;
import com.sixpack.dorundorun.feature.run.dto.response.RunSessionResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompleteRunSessionService {

	private final FindRunSessionByIdAndUserIdService findRunSessionByIdAndUserIdService;

	@Transactional
	public RunSession complete(Long sessionId, Long userId, CompleteRunRequest request) {
		RunSession runSession = findRunSessionByIdAndUserIdService.find(sessionId, userId);

		runSession.complete(
			request.distance().total(),
			request.duration().total(),
			request.pace().avg(),
			request.pace().max().value(),
			request.pace().max().latitude(),
			request.pace().max().longitude(),
			request.cadence().avg(),
			request.cadence().max().value()
		);

		return runSession;
	}

	public RunSessionResponse toResponse(RunSession runSession) {
		return new RunSessionResponse(
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
			runSession.getCadenceMax()
		);
	}
}
