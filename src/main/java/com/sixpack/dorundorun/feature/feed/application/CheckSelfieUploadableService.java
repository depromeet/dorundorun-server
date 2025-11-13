package com.sixpack.dorundorun.feature.feed.application;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.dto.request.CheckSelfieUploadableRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.CheckSelfieUploadableResponse;
import com.sixpack.dorundorun.feature.run.application.FindRunSessionByIdAndUserIdService;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.utils.KoreaTimeHandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckSelfieUploadableService {

	private final FindRunSessionByIdAndUserIdService findRunSessionByIdAndUserIdService;
	private final FeedJpaRepository feedJpaRepository;
	private final KoreaTimeHandler koreaTimeHandler;

	@Transactional(readOnly = true)
	public CheckSelfieUploadableResponse execute(User user, CheckSelfieUploadableRequest request) {
		RunSession runSession = findRunSessionByIdAndUserIdService.find(request.runSessionId(), user.getId());

		LocalDate koreaToday = koreaTimeHandler.now();

		if (!isRunOnDate(runSession.getCreatedAt(), koreaToday)) {
			return CheckSelfieUploadableResponse.notUploadable("RUN_NOT_TODAY");
		}

		if (hasUploadedOnDate(user.getId(), koreaToday)) {
			return CheckSelfieUploadableResponse.notUploadable("ALREADY_UPLOADED_TODAY");
		}

		return CheckSelfieUploadableResponse.uploadable();
	}

	private boolean isRunOnDate(LocalDateTime runStartTime, LocalDate baseDate) {
		LocalDate runStartKoreaDate = koreaTimeHandler.toKoreaDate(runStartTime);
		boolean isRunOnDate = baseDate.equals(runStartKoreaDate);
		log.debug("[시간 처리 로그] 기준 날짜({})에 러닝했는가? {}", baseDate, isRunOnDate);
		return isRunOnDate;
	}

	private boolean hasUploadedOnDate(Long userId, LocalDate baseDate) {
		LocalDateTime dateStartInUtc = koreaTimeHandler.startOfDayInUtc(baseDate);
		LocalDateTime dateEndInUtc = koreaTimeHandler.endOfDayInUtc(baseDate);

		boolean hasUploaded = feedJpaRepository.findByUserIdAndDateRangeWithReactions(
			userId,
			userId,
			dateStartInUtc,
			dateEndInUtc,
			org.springframework.data.domain.PageRequest.of(0, 1)
		).hasContent();

		log.debug("[시간 처리 로그] 기준 날짜({})에 이미 업로드했는가? {}", baseDate, hasUploaded);
		return hasUploaded;
	}
}
