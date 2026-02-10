package com.sixpack.dorundorun.feature.run.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.run.dao.RunSessionJpaRepository;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.run.dto.request.ManualRunSessionCompleteRequest;
import com.sixpack.dorundorun.feature.run.dto.response.ManualRunSessionCompleteResponse;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateRunSessionManualService {

	private final RunSessionJpaRepository runSessionJpaRepository;
	private final FindUserByIdService findUserByIdService;

	@Transactional
	public ManualRunSessionCompleteResponse create(Long userId, ManualRunSessionCompleteRequest request) {
		User user = findUserByIdService.find(userId);

		RunSession runSession = RunSession.builder()
			.user(user)
			.finishedAt(request.startedAt().plusSeconds(request.durationTotal()))
			.distanceTotal(request.distanceTotal())
			.durationTotal(request.durationTotal())
			.paceAvg(request.paceAvg().longValue())
			.cadenceAvg(request.cadenceAvg())
			.manual(true)
			.build();

		runSessionJpaRepository.save(runSession);

		return new ManualRunSessionCompleteResponse(
			runSession.getId(),
			runSession.getFinishedAt(),
			runSession.getDurationTotal(),
			runSession.getDistanceTotal(),
			request.paceAvg(),
			runSession.getCadenceAvg()
		);
	}
}
