package com.sixpack.dorundorun.feature.run.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.run.dao.RunSessionJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindUserIdsWithNoRecentRunService {

	private final RunSessionJpaRepository runSessionRepository;

	@Transactional(readOnly = true)
	public List<Long> findUserIdsWithNoRecentRun() {
		return runSessionRepository.findUserIdsWithNoRecentRun();
	}
}
