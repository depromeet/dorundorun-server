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

		LocalDateTime runStartTime = runSession.getCreatedAt();
		LocalDate koreaToday = koreaTimeHandler.now();
		LocalDate runStartKoreaDate = koreaTimeHandler.toKoreaDate(runStartTime);
		log.info("[시간 처리 로그] 오늘 러닝인가? {}", koreaTimeHandler.isTodayInKorea(runStartTime));

		if (!koreaTimeHandler.isTodayInKorea(runStartTime)) {
			return CheckSelfieUploadableResponse.notUploadable("RUN_NOT_TODAY");
		}

		LocalDateTime todayStartInUtc = koreaTimeHandler.todayStartInUtc();
		LocalDateTime todayEndInUtc = koreaTimeHandler.todayEndInUtc();

		boolean hasUploadedToday = feedJpaRepository.findByUserIdAndDateRangeWithReactions(
			user.getId(),
			user.getId(),
			todayStartInUtc,
			todayEndInUtc,
			org.springframework.data.domain.PageRequest.of(0, 1)
		).hasContent();

		log.info("[시간 처리 로그] 오늘 이미 업로드했는가? {}", hasUploadedToday);

		if (hasUploadedToday) {
			return CheckSelfieUploadableResponse.notUploadable("ALREADY_UPLOADED_TODAY");
		}

		return CheckSelfieUploadableResponse.uploadable();
	}
}
