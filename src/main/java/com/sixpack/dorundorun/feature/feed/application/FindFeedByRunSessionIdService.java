package com.sixpack.dorundorun.feature.feed.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFeedByRunSessionIdService {

	private final FeedJpaRepository feedJpaRepository;

	@Transactional(readOnly = true)
	public Optional<Feed> findOrNull(Long sessionId) {
		return feedJpaRepository.findByRunSessionIdAndDeletedAtIsNull(sessionId);
	}
}
