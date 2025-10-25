package com.sixpack.dorundorun.feature.feed.application;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindAllFeedsWithReactionsByUserIdAndDateRangeService {

	private final FeedJpaRepository feedJpaRepository;

	@Transactional(readOnly = true)
	public Page<Feed> find(Long userId, Long currentUserId, boolean isFriendFeed, LocalDateTime startOfDay,
		LocalDateTime endOfDay, Pageable pageable) {
		return feedJpaRepository.findByUserIdAndDateRangeWithReactions(
			userId, currentUserId, isFriendFeed, startOfDay, endOfDay, pageable
		);
	}
}
