package com.sixpack.dorundorun.feature.run.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.run.dao.RunSegmentJpaRepository;
import com.sixpack.dorundorun.feature.run.domain.RunSegment;
import com.sixpack.dorundorun.feature.run.domain.RunSegments;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindAllRunSegmentsByRunSessionIdService {

	private final RunSegmentJpaRepository runSegmentJpaRepository;

	@Transactional(readOnly = true)
	public RunSegments find(Long sessionId) {
		List<RunSegment> segments = runSegmentJpaRepository.findByRunSessionIdOrderByCreatedAtAsc(sessionId);
		return new RunSegments(segments);
	}
}
