package com.sixpack.dorundorun.feature.run.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.run.dao.RunSessionJpaRepository;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.run.exception.RunErrorCode;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateRunSessionService {

	private final FindUserByIdService findUserByIdService;
	private final FindActiveRunSessionService findActiveRunSessionService;
	private final RunSessionJpaRepository runSessionJpaRepository;

	@Transactional
	public RunSession create(Long userId) {
		User user = findUserByIdService.find(userId);

		validateNoActiveRunSession(userId);

		RunSession runSession = createNewRunSession(user);
		return runSessionJpaRepository.save(runSession);
	}

	private void validateNoActiveRunSession(Long userId) {
		findActiveRunSessionService.find(userId).ifPresent(activeSession -> {
			throw RunErrorCode.ALREADY_EXISTS_ACTIVE_RUN_SESSION.format(activeSession.getId());
		});
	}

	private RunSession createNewRunSession(User user) {
		return RunSession.builder()
			.user(user)
			.build();
	}
}
