package com.sixpack.dorundorun.feature.feed.application;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.friend.dao.FriendJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFeedsByDateRangeService {

	private final FeedJpaRepository feedJpaRepository;
	private final FriendJpaRepository friendJpaRepository;

	@Transactional(readOnly = true)
	public List<Feed> find(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
		if (userId != null) {
			// 특정 유저의 피드 조회 (마이페이지/친구페이지)
			return feedJpaRepository.findByUserIdAndDateRange(userId, startOfDay, endOfDay);
		} else {
			return Collections.emptyList();
		}
	}

	@Transactional(readOnly = true)
	public List<Feed> findFriendFeeds(Long currentUserId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
		// 1. 현재 유저의 친구 ID 목록 조회
		List<Long> friendIds = friendJpaRepository.findFriendIdsByUserId(currentUserId);

		// 2. 친구가 없으면 빈 리스트 반환
		if (friendIds.isEmpty()) {
			return Collections.emptyList();
		}

		// 3. 친구들의 피드 조회
		return feedJpaRepository.findByFriendIdsAndDateRange(friendIds, startOfDay, endOfDay);
	}
}
