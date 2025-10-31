package com.sixpack.dorundorun.feature.feed.application;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieUsersRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieUsersResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieUsersResponse.PostingUserState;
import com.sixpack.dorundorun.feature.user.application.GetDefaultProfileImageUrlService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindSelfieUsersByDateService {

	private final FeedJpaRepository feedJpaRepository;
	private final S3Service s3Service;
	private final GetDefaultProfileImageUrlService getDefaultProfileImageUrlService;

	@Transactional(readOnly = true)
	public SelfieUsersResponse find(User currentUser, SelfieUsersRequest request) {
		LocalDateTime startOfDay = request.date().atStartOfDay();
		LocalDateTime endOfDay = request.date().atTime(LocalTime.MAX);

		List<Feed> feeds = feedJpaRepository.findByCurrentUserAndFriendsAndDateRange(
			currentUser.getId(),
			startOfDay,
			endOfDay
		);

		List<PostingUserState> users = feeds.stream()
			.map(feed -> convertToPostingUserState(feed, currentUser.getId()))
			.toList();

		return new SelfieUsersResponse(users);
	}

	private PostingUserState convertToPostingUserState(Feed feed, Long currentUserId) {
		User user = feed.getUser();
		String profileImageUrl = user.getProfileImageUrl() != null
			? s3Service.getImageUrl(user.getProfileImageUrl())
			: getDefaultProfileImageUrlService.get();

		return new PostingUserState(
			user.getId(),
			user.getNickname(),
			profileImageUrl,
			feed.getCreatedAt(),
			user.getId().equals(currentUserId)
		);
	}
}