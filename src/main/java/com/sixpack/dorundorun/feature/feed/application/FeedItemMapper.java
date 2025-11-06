package com.sixpack.dorundorun.feature.feed.application;

import java.util.List;

import org.springframework.stereotype.Component;

import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.domain.Reaction;
import com.sixpack.dorundorun.feature.feed.domain.ReactionsByEmoji;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse.FeedItem;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse.ReactionSummary;
import com.sixpack.dorundorun.feature.user.application.GetDefaultProfileImageUrlService;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FeedItemMapper {

	private final S3Service s3Service;
	private final GetDefaultProfileImageUrlService getDefaultProfileImageUrlService;

	public FeedItem toFeedItem(Feed feed, Long currentUserId) {
		List<Reaction> activeReactions = feed.getActiveReactions();
		ReactionsByEmoji reactionsByEmoji = ReactionsByEmoji.from(activeReactions);
		List<ReactionSummary> reactionSummaries = reactionsByEmoji.toReactionSummaries(currentUserId);

		List<ReactionSummary> convertedReactionSummaries = reactionSummaries.stream()
			.map(summary -> convertReactionSummaryUrls(summary, currentUserId))
			.toList();

		String profileImageUrl = feed.getUser().getProfileImageUrl() != null
			? s3Service.getImageUrl(feed.getUser().getProfileImageUrl())
			: getDefaultProfileImageUrlService.get();

		String selfieImageUrl = feed.getSelfieImage() != null
			? feed.getSelfieImageUrl()
			: feed.getMapImageUrl();

		return FeedItem.of(feed, convertedReactionSummaries, currentUserId, profileImageUrl, selfieImageUrl);
	}

	private ReactionSummary convertReactionSummaryUrls(ReactionSummary summary, Long currentUserId) {
		List<com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse.ReactionUser> convertedUsers = summary.users()
			.stream()
			.map(user -> new com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse.ReactionUser(
				user.userId(),
				user.nickname(),
				user.profileImageUrl() != null ? s3Service.getImageUrl(user.profileImageUrl()) :
					getDefaultProfileImageUrlService.get(),
				user.userId().equals(currentUserId),
				user.reactedAt()
			))
			.toList();

		return new ReactionSummary(
			summary.emojiType(),
			summary.totalCount(),
			summary.isReactedByMe(),
			convertedUsers
		);
	}
}
