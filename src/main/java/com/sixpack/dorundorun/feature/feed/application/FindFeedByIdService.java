package com.sixpack.dorundorun.feature.feed.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse.FeedItem;
import com.sixpack.dorundorun.feature.feed.exception.FeedErrorCode;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFeedByIdService {

	private final FeedJpaRepository feedJpaRepository;
	private final FeedItemMapper feedItemMapper;

	@Transactional(readOnly = true)
	public FeedItem find(Long feedId, User currentUser) {
		Feed feed = feedJpaRepository.findByIdWithUserAndRunSession(feedId)
			.orElseThrow(() -> FeedErrorCode.NOT_FOUND_FEED.format(feedId));

		return feedItemMapper.toFeedItem(feed, currentUser.getId());
	}
}
