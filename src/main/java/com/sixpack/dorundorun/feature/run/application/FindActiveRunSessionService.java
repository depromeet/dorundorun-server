package com.sixpack.dorundorun.feature.run.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.run.dao.RunSessionJpaRepository;
import com.sixpack.dorundorun.feature.run.domain.RunSession;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindActiveRunSessionService {

	private final RunSessionJpaRepository runSessionJpaRepository;

	@Transactional(readOnly = true)
	public Optional<RunSession> find(Long userId) {
		return runSessionJpaRepository.findByUserIdAndFinishedAtIsNull(userId);
	}
}
