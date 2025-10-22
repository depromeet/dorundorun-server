package com.sixpack.dorundorun.feature.feed.application;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.ReactionJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Reaction;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindReactionsByFeedIdsService {

	private final ReactionJpaRepository reactionJpaRepository;

	@Transactional(readOnly = true)
	public Map<Long, List<Reaction>> find(List<Long> feedIds) {
		if (feedIds.isEmpty()) {
			return Map.of();
		}

		return reactionJpaRepository.findByFeedIdIn(feedIds).stream()
			.collect(Collectors.groupingBy(r -> r.getFeed().getId()));
	}
}
