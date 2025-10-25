package com.sixpack.dorundorun.feature.feed.domain;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse.ReactionUser;

public record ReactionsByEmoji(
	Map<String, List<Reaction>> reactionMap
) {

	public static ReactionsByEmoji from(List<Reaction> reactions) {
		Map<String, List<Reaction>> reactionsByEmoji = reactions.stream()
			.collect(Collectors.groupingBy(r -> r.getEmojiType().name()));
		return new ReactionsByEmoji(reactionsByEmoji);
	}

	public List<SelfieFeedResponse.ReactionSummary> toReactionSummaries() {
		return reactionMap.entrySet().stream()
			.map(entry -> createReactionSummary(entry.getKey(), entry.getValue()))
			.toList();
	}

	private SelfieFeedResponse.ReactionSummary createReactionSummary(
		String emojiType, List<Reaction> reactions) {

		List<ReactionUser> reactionUsers = reactions.stream()
			.map(ReactionUser::from)
			.toList();

		return new SelfieFeedResponse.ReactionSummary(
			emojiType,
			reactions.size(),
			reactionUsers
		);
	}
}
