package com.sixpack.dorundorun.feature.run.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.run.dao.RunSessionJpaRepository;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.run.exception.RunErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindRunSessionByIdAndUserIdService {

	private final RunSessionJpaRepository runSessionJpaRepository;

	@Transactional(readOnly = true)
	public RunSession find(Long sessionId, Long userId) {
		return runSessionJpaRepository.findByIdAndUserId(sessionId, userId)
			.orElseThrow(() -> RunErrorCode.NOT_FOUND_RUN_SESSION.format(sessionId));
	}
}
