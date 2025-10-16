package com.sixpack.dorundorun.feature.run.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.run.dao.RunSessionJpaRepository;
import com.sixpack.dorundorun.feature.run.dao.projection.RunSessionWithFeedProjection;
import com.sixpack.dorundorun.feature.run.dto.request.RunSessionListRequest;
import com.sixpack.dorundorun.feature.run.dto.response.RunSessionListResponse;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindAllRunSessionsService {

	private final RunSessionJpaRepository runSessionJpaRepository;
	private final S3Service s3Service;

	@Transactional(readOnly = true)
	public List<RunSessionListResponse> find(Long userId, RunSessionListRequest request) {
		List<RunSessionWithFeedProjection> results = runSessionJpaRepository.findAllBySelfiedStatusAndStartDateTime(
			userId,
			request.isSelfied(),
			request.startDateTime()
		);

		return results.stream()
			.map(this::mapToResponse)
			.toList();
	}

	private RunSessionListResponse mapToResponse(RunSessionWithFeedProjection projection) {
		return new RunSessionListResponse(
			projection.getId(),
			projection.getCreatedAt(),
			projection.getUpdatedAt(),
			projection.getFinishedAt(),
			projection.getDistanceTotal(),
			projection.getDurationTotal(),
			projection.getPaceAvg(),
			projection.getCadenceAvg(),
			projection.getIsSelfied(),
			s3Service.getImageUrl(projection.getMapImage())
		);
	}
}
