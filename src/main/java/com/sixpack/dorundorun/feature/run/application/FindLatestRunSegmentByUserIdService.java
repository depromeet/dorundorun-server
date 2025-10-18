package com.sixpack.dorundorun.feature.run.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.run.dao.RunSegmentJpaRepository;
import com.sixpack.dorundorun.feature.run.domain.RunSegment;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindLatestRunSegmentByUserIdService {

	private final RunSegmentJpaRepository runSegmentJpaRepository;

	// 특정 유저의 가장 최신 RunSegment 조회
	@Transactional(readOnly = true)
	public Optional<RunSegment> find(Long userId) {
		return runSegmentJpaRepository.findTopByRunSessionUserIdOrderByCreatedAtDesc(userId);
	}
}
