package com.sixpack.dorundorun.feature.feed.application;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFeedsByDateRangeService {

	private final FeedJpaRepository feedJpaRepository;

	@Transactional(readOnly = true)
	public List<Feed> find(Long userId, LocalDateTime startOfDay, LocalDateTime endOfDay) {
		if (userId != null) {
			// 특정 유저의 피드 조회 (마이페이지/친구페이지)
			return feedJpaRepository.findByUserIdAndDateRange(userId, startOfDay, endOfDay);
		} else {
			// 모든 유저의 피드 조회 (홈 피드)
			// TODO: Friend 관계 연동 후 친구들의 피드만 조회하도록 수정
			return feedJpaRepository.findByDateRange(startOfDay, endOfDay);
		}
	}
}
