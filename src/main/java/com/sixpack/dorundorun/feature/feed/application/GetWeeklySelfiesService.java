package com.sixpack.dorundorun.feature.feed.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieWeekListRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieWeekResponse;
import com.sixpack.dorundorun.feature.friend.dao.FriendJpaRepository;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GetWeeklySelfiesService {

	private final FriendJpaRepository friendJpaRepository;
	private final FeedJpaRepository feedJpaRepository;

	@Transactional(readOnly = true)
	public SelfieWeekResponse execute(User user, SelfieWeekListRequest request) {
		// 1. 사용자의 친구 ID 목록 조회
		List<Long> friendIds = friendJpaRepository.findFriendIdsByUserId(user.getId());

		// 2. 친구가 없으면 빈 응답 반환
		if (friendIds.isEmpty()) {
			return createEmptyResponse(request.getStartDate(), request.getEndDate());
		}

		// 3. 기간 내 친구들의 날짜별 인증 수 조회
		LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
		LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

		List<Object[]> results = feedJpaRepository.countFeedsByFriendsAndDateRange(
			friendIds,
			startDateTime,
			endDateTime
		);

		// 4. 결과를 Map으로 변환 (날짜 -> 카운트)
		Map<LocalDate, Long> dateCountMap = results.stream()
			.collect(Collectors.toMap(
				row -> ((java.sql.Date)row[0]).toLocalDate(),
				row -> ((Number)row[1]).longValue()
			));

		// 5. 모든 날짜에 대해 응답 생성 (데이터 없는 날짜는 0)
		List<SelfieWeekResponse.DailyCertification> data = new ArrayList<>();
		LocalDate currentDate = request.getStartDate();

		while (!currentDate.isAfter(request.getEndDate())) {
			Long count = dateCountMap.getOrDefault(currentDate, 0L);
			data.add(new SelfieWeekResponse.DailyCertification(
				currentDate.toString(),
				count.intValue()
			));
			currentDate = currentDate.plusDays(1);
		}
		return new SelfieWeekResponse(data);
	}

	private SelfieWeekResponse createEmptyResponse(LocalDate startDate, LocalDate endDate) {
		List<SelfieWeekResponse.DailyCertification> data = new ArrayList<>();
		LocalDate currentDate = startDate;

		while (!currentDate.isAfter(endDate)) {
			data.add(new SelfieWeekResponse.DailyCertification(
				currentDate.toString(),
				0
			));
			currentDate = currentDate.plusDays(1);
		}

		return new SelfieWeekResponse(data);
	}
}
