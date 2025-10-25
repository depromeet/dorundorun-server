package com.sixpack.dorundorun.feature.feed.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.DailyCertifications;
import com.sixpack.dorundorun.feature.feed.dto.projection.FeedCountByDateProjection;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieWeekListRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieWeekResponse;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FindAllWeeklySelfiesService {

	private final FeedJpaRepository feedJpaRepository;

	@Transactional(readOnly = true)
	public SelfieWeekResponse execute(User user, SelfieWeekListRequest request) {
		LocalDateTime startDateTime = request.startDate() != null ? request.startDate().atStartOfDay() : null;
		LocalDateTime endDateTime = request.endDate() != null ? request.endDate().atTime(23, 59, 59) : null;

		List<FeedCountByDateProjection> queryResults = feedJpaRepository.countFriendFeedsByDateRange(
			user.getId(), startDateTime, endDateTime);

		if (queryResults.isEmpty()) {
			return createEmptyResponse(request.startDate(), request.endDate());
		}

		DailyCertifications certifications = DailyCertifications.from(queryResults);
		List<SelfieWeekResponse.DailyCertification> data = certifications.toResponseList(
			request.startDate(), request.endDate());
		
		return new SelfieWeekResponse(data);
	}

	private SelfieWeekResponse createEmptyResponse(LocalDate startDate, LocalDate endDate) {
		List<SelfieWeekResponse.DailyCertification> data = DailyCertifications.createEmptyList(startDate, endDate);
		return new SelfieWeekResponse(data);
	}
}
