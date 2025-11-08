package com.sixpack.dorundorun.feature.run.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.run.dao.RunSessionJpaRepository;
import com.sixpack.dorundorun.feature.run.dao.RunSegmentJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteRunSessionByIdService {

	private final RunSessionJpaRepository runSessionJpaRepository;
	private final RunSegmentJpaRepository runSegmentJpaRepository;

	@Transactional
	public void delete(Long id) {
		runSegmentJpaRepository.deleteByRunSessionId(id);
		runSessionJpaRepository.deleteById(id);
	}
}
