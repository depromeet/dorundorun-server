package com.sixpack.dorundorun.feature.feed.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.dto.request.CheckSelfieUploadableRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.CheckSelfieUploadableResponse;
import com.sixpack.dorundorun.feature.run.application.FindRunSessionByIdAndUserIdService;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckSelfieUploadableService {

	private final FindRunSessionByIdAndUserIdService findRunSessionByIdAndUserIdService;
	private final FeedJpaRepository feedJpaRepository;

	@Transactional(readOnly = true)
	public CheckSelfieUploadableResponse execute(User user, CheckSelfieUploadableRequest request) {
		RunSession runSession = findRunSessionByIdAndUserIdService.find(request.runSessionId(), user.getId());

		LocalDateTime runStartTime = runSession.getCreatedAt();
		LocalDate runStartDate = runStartTime.toLocalDate();
		LocalDate today = LocalDate.now();

		if (!runStartDate.equals(today)) {
			return CheckSelfieUploadableResponse.notUploadable("RUN_NOT_TODAY");
		}

		LocalDateTime todayStart = today.atStartOfDay();
		LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

		boolean hasUploadedToday = feedJpaRepository.findByUserIdAndDateRangeWithReactions(
			user.getId(),
			user.getId(),
			todayStart,
			todayEnd,
			org.springframework.data.domain.PageRequest.of(0, 1)
		).hasContent();

		if (hasUploadedToday) {
			return CheckSelfieUploadableResponse.notUploadable("ALREADY_UPLOADED_TODAY");
		}

		return CheckSelfieUploadableResponse.uploadable();
	}
}
