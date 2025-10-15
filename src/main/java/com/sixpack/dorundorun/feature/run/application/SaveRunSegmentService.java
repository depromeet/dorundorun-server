package com.sixpack.dorundorun.feature.run.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.run.dao.RunSegmentJpaRepository;
import com.sixpack.dorundorun.feature.run.domain.RunSegment;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentInfo;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.run.dto.request.SaveRunSegmentDataRequest;
import com.sixpack.dorundorun.feature.run.dto.request.SaveRunSegmentRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SaveRunSegmentService {

	private final FindRunSessionByIdAndUserIdService findRunSessionByIdAndUserIdService;
	private final RunSegmentJpaRepository runSegmentJpaRepository;

	@Transactional
	public RunSegment save(Long sessionId, Long userId, SaveRunSegmentRequest request) {
		RunSession runSession = findRunSessionByIdAndUserIdService.find(sessionId, userId);

		RunSegmentInfo segmentInfo = convertToSegmentInfo(request);

		RunSegment runSegment = RunSegment.builder()
			.runSession(runSession)
			.data(segmentInfo)
			.build();

		return runSegmentJpaRepository.save(runSegment);
	}

	private RunSegmentInfo convertToSegmentInfo(SaveRunSegmentRequest request) {
		List<RunSegmentData> segments = request.segments().stream()
			.map(this::convertToSegmentData)
			.toList();

		return new RunSegmentInfo(
			segments,
			request.isStopped()
		);
	}

	private RunSegmentData convertToSegmentData(SaveRunSegmentDataRequest dataRequest) {
		return new RunSegmentData(
			dataRequest.time(),
			dataRequest.latitude(),
			dataRequest.longitude(),
			dataRequest.altitude(),
			dataRequest.distance(),
			dataRequest.pace(),
			dataRequest.speed(),
			dataRequest.cadence()
		);
	}
}
