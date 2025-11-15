package com.sixpack.dorundorun.feature.auth.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.dao.ReactionJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.friend.dao.FriendJpaRepository;
import com.sixpack.dorundorun.feature.notification.dao.NotificationJpaRepository;
import com.sixpack.dorundorun.feature.run.dao.RunSegmentJpaRepository;
import com.sixpack.dorundorun.feature.run.dao.RunSessionJpaRepository;
import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.redis.token.RedisTokenRepository;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawService {

	private final UserJpaRepository userJpaRepository;
	private final FeedJpaRepository feedJpaRepository;
	private final ReactionJpaRepository reactionJpaRepository;
	private final FriendJpaRepository friendJpaRepository;
	private final RunSessionJpaRepository runSessionJpaRepository;
	private final RunSegmentJpaRepository runSegmentJpaRepository;
	private final NotificationJpaRepository notificationJpaRepository;
	private final RedisTokenRepository redisTokenRepository;
	private final S3Service s3Service;

	@Transactional
	public void withdraw(User user) {
		Long userId = user.getId();
		deleteFeedImages(userId);
		deleteReactions(userId);
		deleteFeeds(userId);
		deleteFriends(userId);
		deleteRunSegments(userId);
		deleteRunSessions(userId);
		deleteNotifications(userId);
		deleteProfileImage(user);
		redisTokenRepository.delete(userId);
		userJpaRepository.delete(user);
	}

	private void deleteReactions(Long userId) {
		reactionJpaRepository.deleteByUserId(userId);
	}

	private void deleteFeeds(Long userId) {
		feedJpaRepository.deleteByUserId(userId);
	}

	private void deleteFriends(Long userId) {
		friendJpaRepository.deleteByUserId(userId);
		friendJpaRepository.deleteByFriendId(userId);
	}

	private void deleteRunSegments(Long userId) {
		runSegmentJpaRepository.deleteByUserId(userId);
	}

	private void deleteRunSessions(Long userId) {
		runSessionJpaRepository.deleteByUserId(userId);
	}

	private void deleteNotifications(Long userId) {
		notificationJpaRepository.deleteByRecipientUserId(userId);
	}

	private void deleteFeedImages(Long userId) {
		List<Feed> feeds = feedJpaRepository.findAllByUserId(userId);
		for (Feed feed : feeds) {
			if (feed.getMapImage() != null && !feed.getMapImage().isEmpty()) {
				try {
					s3Service.deleteImage(feed.getMapImage());
				} catch (Exception e) {
					log.warn("Failed to delete feed map image: {}", e.getMessage());
				}
			}
			if (feed.getSelfieImage() != null && !feed.getSelfieImage().isEmpty()) {
				try {
					s3Service.deleteImage(feed.getSelfieImage());
				} catch (Exception e) {
					log.warn("Failed to delete feed selfie image: {}", e.getMessage());
				}
			}
		}
	}

	private void deleteProfileImage(User user) {
		String profileImageUrl = user.getProfileImageUrl();
		if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
			try {
				s3Service.deleteImage(profileImageUrl);
			} catch (Exception e) {
				log.warn("Failed to delete profile image: {}", e.getMessage());
			}
		}
	}
}
