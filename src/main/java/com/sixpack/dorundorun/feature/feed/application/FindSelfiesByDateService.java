package com.sixpack.dorundorun.feature.feed.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.dto.request.FeedListRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse.FeedItem;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse.UserSummary;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.response.PaginationResponse;
import com.sixpack.dorundorun.global.utils.KoreaTimeHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindSelfiesByDateService {

	private final FindAllFeedsWithReactionsByUserIdAndDateRangeService findAllFeedsWithReactionsByUserIdAndDateRangeService;
	private final FindUserSummaryService findUserSummaryService;
	private final FeedItemMapper feedItemMapper;
	private final KoreaTimeHandler koreaTimeHandler;

	@Transactional(readOnly = true)
	public SelfieFeedResponse find(User currentUser, FeedListRequest request) {
		Pageable pageable = PageRequest.of(request.page(), request.size());
		Page<Feed> feedsPage = loadFeedsByCondition(
			request.userId(), currentUser.getId(), request.currentDate(), pageable);

		List<FeedItem> feedItems = feedsPage.getContent().stream()
			.map(feed -> feedItemMapper.toFeedItem(feed, currentUser.getId()))
			.toList();

		SelfieFeedResponse.UserSummary userSummary = loadUserSummary(request.userId());
		PaginationResponse<FeedItem> feedsPagination = PaginationResponse.of(
			feedItems, request.page(), request.size(), feedsPage.getTotalElements());

		return new SelfieFeedResponse(userSummary, feedsPagination);
	}

	private UserSummary loadUserSummary(Long userId) {
		return userId != null
			? findUserSummaryService.find(userId)
			: null;
	}

	private Page<Feed> loadFeedsByCondition(Long userId, Long currentUserId, LocalDate targetDate, Pageable pageable) {
		LocalDateTime startOfDayInUtc = targetDate != null ? koreaTimeHandler.startOfDayInUtc(targetDate) : null;
		LocalDateTime endOfDayInUtc = targetDate != null ? koreaTimeHandler.endOfDayInUtc(targetDate) : null;

		// userId가 null이면 나와 친구들의 피드 조회, null이 아니면 해당 유저의 피드만 조회
		return findAllFeedsWithReactionsByUserIdAndDateRangeService.find(userId, currentUserId, startOfDayInUtc,
			endOfDayInUtc, pageable);
	}
}
