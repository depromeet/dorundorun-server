package com.sixpack.dorundorun.feature.feed.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.dao.ReactionJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.domain.Reaction;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieReactionRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.ReactionAction;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieReactionResponse;
import com.sixpack.dorundorun.feature.feed.event.FeedReactionRequestedEvent;
import com.sixpack.dorundorun.feature.feed.exception.FeedErrorCode;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReactToSelfieService {

	private final FeedJpaRepository feedJpaRepository;
	private final ReactionJpaRepository reactionJpaRepository;
	private final RedisStreamPublisher redisStreamPublisher;

	@Transactional
	public SelfieReactionResponse execute(User user, SelfieReactionRequest request) {
		Feed feed = feedJpaRepository.findById(request.feedId())
			.filter(f -> f.getDeletedAt() == null)
			.orElseThrow(() -> FeedErrorCode.NOT_FOUND_FEED.format(request.feedId()));

		Optional<Reaction> existingReaction = reactionJpaRepository.findByFeedIdAndUserIdAndEmojiType(
			request.feedId(),
			user.getId(),
			request.emojiType()
		);

		ReactionAction action = toggleReaction(existingReaction, user, feed, request);
		int totalReactionCount = reactionJpaRepository.countByFeedIdAndDeletedAtIsNull(request.feedId());

		// Reaction이 추가된 경우 알림 발송
		if (action == ReactionAction.ADDED) {
			FeedReactionRequestedEvent event = FeedReactionRequestedEvent.builder()
				.feedId(request.feedId())
				.reactorId(user.getId())
				.feedOwnerId(feed.getUser().getId())
				.build();
			redisStreamPublisher.publishAfterCommit(event);
		}

		return new SelfieReactionResponse(
			request.feedId(),
			request.emojiType().name(),
			action,
			totalReactionCount
		);
	}

	private ReactionAction toggleReaction(Optional<Reaction> existingReaction, User user, Feed feed,
		SelfieReactionRequest request) {
		if (existingReaction.isPresent()) {
			Reaction reaction = existingReaction.get();

			if (reaction.isActive()) {
				reaction.deactivate();
				return ReactionAction.REMOVED;
			} else {
				reaction.activate();
				return ReactionAction.ADDED;
			}
		} else {
			Reaction newReaction = Reaction.builder()
				.user(user)
				.feed(feed)
				.emojiType(request.emojiType())
				.build();
			reactionJpaRepository.save(newReaction);
			return ReactionAction.ADDED;
		}
	}
}
