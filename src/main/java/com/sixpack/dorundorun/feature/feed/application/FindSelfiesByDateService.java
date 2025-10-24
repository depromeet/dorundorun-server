package com.sixpack.dorundorun.feature.feed.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.domain.Reaction;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindSelfiesByDateService {

	private final FindFeedsByDateRangeService findFeedsByDateRangeService;
	private final FindUserSummaryService findUserSummaryService;
	private final FindReactionsByFeedIdsService findReactionsByFeedIdsService;

	@Transactional(readOnly = true)
	public SelfieFeedResponse find(LocalDate targetDate, Long userId, User currentUser) {
		LocalDateTime startOfDay = targetDate.atStartOfDay();
		LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

		// 1. 피드 조회
		List<Feed> feeds = findFeedsByDateRangeService.find(userId, startOfDay, endOfDay);

		// 2. UserSummary 생성 (userId가 있을 때만)
		SelfieFeedResponse.UserSummary userSummary = userId != null
			? findUserSummaryService.find(userId)
			: null;

		// 3. 모든 피드의 반응 조회
		List<Long> feedIds = feeds.stream()
			.map(Feed::getId)
			.collect(Collectors.toList());

		Map<Long, List<Reaction>> reactionsByFeedId = findReactionsByFeedIdsService.find(feedIds);

		// 4. FeedItem 목록 생성
		List<SelfieFeedResponse.FeedItem> feedItems = feeds.stream()
			.map(feed -> createFeedItem(feed, reactionsByFeedId.getOrDefault(feed.getId(), List.of())))
			.collect(Collectors.toList());

		return new SelfieFeedResponse(userSummary, feedItems);
	}

	private SelfieFeedResponse.FeedItem createFeedItem(Feed feed, List<Reaction> reactions) {
		// Reaction을 이모지 타입별로 그룹핑
		Map<String, List<Reaction>> reactionsByEmoji = reactions.stream()
			.collect(Collectors.groupingBy(r -> r.getEmojiType().name()));

		// ReactionSummary 생성
		List<SelfieFeedResponse.ReactionSummary> reactionSummaries = reactionsByEmoji.entrySet().stream()
			.map(entry -> {
				String emojiType = entry.getKey();
				List<Reaction> emojiReactions = entry.getValue();

				// ReactionUser 목록 생성
				List<SelfieFeedResponse.ReactionUser> reactionUsers = emojiReactions.stream()
					.map(r -> new SelfieFeedResponse.ReactionUser(
						r.getUser().getId(),
						r.getUser().getNickname(),
						r.getUser().getProfileImageUrl(),
						r.getCreatedAt()
					))
					.collect(Collectors.toList());

				return new SelfieFeedResponse.ReactionSummary(
					emojiType,
					emojiReactions.size(),
					reactionUsers
				);
			})
			.collect(Collectors.toList());

		// FeedItem 생성
		return new SelfieFeedResponse.FeedItem(
			feed.getId(),
			feed.getCreatedAt().toLocalDate().toString(),
			feed.getUser().getNickname(),
			feed.getUser().getProfileImageUrl(),
			feed.getCreatedAt(),
			feed.getRunSession().getDistanceTotal(),
			feed.getRunSession().getDurationTotal(),
			feed.getRunSession().getPaceAvg(),
			feed.getRunSession().getCadenceAvg(),
			feed.getSelfieImageUrl(),
			reactionSummaries
		);
	}
}
