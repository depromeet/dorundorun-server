package com.sixpack.dorundorun.feature.feed.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.domain.Reaction;
import com.sixpack.dorundorun.feature.feed.domain.ReactionsByEmoji;
import com.sixpack.dorundorun.feature.feed.dto.request.FeedListRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse.FeedItem;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse.ReactionSummary;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse.UserSummary;
import com.sixpack.dorundorun.global.response.PaginationResponse;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindSelfiesByDateService {

	private final FindAllFeedsWithReactionsByUserIdAndDateRangeService findAllFeedsWithReactionsByUserIdAndDateRangeService;
	private final FindUserSummaryService findUserSummaryService;

	@Transactional(readOnly = true)
	public PaginationResponse<SelfieFeedResponse> find(User currentUser, FeedListRequest request) {
		Pageable pageable = PageRequest.of(request.page(), request.size());
		Page<Feed> feedsPage = loadFeedsByCondition(
			request.userId(), currentUser.getId(), request.currentDate(), pageable);

		List<FeedItem> feedItems = feedsPage.getContent().stream()
			.map(this::createFeedItem)
			.toList();

		SelfieFeedResponse.UserSummary userSummary = loadUserSummary(request.userId());
		SelfieFeedResponse response = new SelfieFeedResponse(userSummary, feedItems);
		
		return PaginationResponse.of(List.of(response), request.page(), request.size(), feedsPage.getTotalElements());
	}

	private UserSummary loadUserSummary(Long userId) {
		return userId != null
			? findUserSummaryService.find(userId)
			: null;
	}

	private Page<Feed> loadFeedsByCondition(Long userId, Long currentUserId, LocalDate targetDate, Pageable pageable) {
		LocalDateTime startOfDay = targetDate != null ? targetDate.atStartOfDay() : null;
		LocalDateTime endOfDay = targetDate != null ? targetDate.atTime(LocalTime.MAX) : null;

		if (userId == null) {
			// userId가 없으면 나와 내 친구들의 피드 조회
			return findAllFeedsWithReactionsByUserIdAndDateRangeService.find(null, currentUserId, true, startOfDay,
				endOfDay, pageable);
		} else if (userId.equals(currentUserId)) {
			// userId가 내 id와 같으면 내 피드 조회
			return findAllFeedsWithReactionsByUserIdAndDateRangeService.find(userId, currentUserId, false, startOfDay,
				endOfDay, pageable);
		} else {
			// userId가 내 id와 다르면 해당 친구의 피드 조회
			return findAllFeedsWithReactionsByUserIdAndDateRangeService.find(userId, currentUserId, false, startOfDay,
				endOfDay, pageable);
		}
	}

	private FeedItem createFeedItem(Feed feed) {
		List<Reaction> reactions = feed.getReactions();
		ReactionsByEmoji reactionsByEmoji = ReactionsByEmoji.from(reactions);
		List<ReactionSummary> reactionSummaries = reactionsByEmoji.toReactionSummaries();

		return FeedItem.of(feed, reactionSummaries);
	}
}
