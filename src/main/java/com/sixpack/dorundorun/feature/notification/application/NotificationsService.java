package com.sixpack.dorundorun.feature.notification.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.notification.dao.NotificationJpaRepository;
import com.sixpack.dorundorun.feature.notification.domain.Notification;
import com.sixpack.dorundorun.feature.notification.domain.NotificationType;
import com.sixpack.dorundorun.feature.notification.dto.response.NotificationResponse;
import com.sixpack.dorundorun.feature.user.application.GetDefaultProfileImageUrlService;
import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsService {

	private final NotificationJpaRepository notificationRepository;
	private final FeedJpaRepository feedRepository;
	private final UserJpaRepository userRepository;
	private final GetDefaultProfileImageUrlService getDefaultProfileImageUrlService;
	private final S3Service s3Service;

	@Transactional(readOnly = true)
	public Page<NotificationResponse> getNotifications(User user, Pageable pageable) {
		log.debug("Fetching notifications for user: {}", user.getId());

		Page<Notification> notifications = notificationRepository.findByUserDeviceTokenAndDeletedAtIsNull(
			user.getDeviceToken(),
			pageable
		);

		log.debug("Found {} notifications for user: {}", notifications.getTotalElements(), user.getId());
		return notifications.map(notification -> buildNotificationResponse(notification));
	}

	private NotificationResponse buildNotificationResponse(Notification notification) {
		String profileImage = getDefaultProfileImageUrlService.get();
		String selfieImage = null;

		if (notification.getType() == NotificationType.CHEER_FRIEND) {
			profileImage = getProfileImageBySenderId(notification);
		} else if (notification.getType() == NotificationType.FEED_REACTION) {
			profileImage = getProfileImageBySenderId(notification);
		} else if (notification.getType() == NotificationType.FEED_UPLOADED) {
			Long feedId = getFeedIdFromNotification(notification);
			if (feedId != null) {
				Feed feed = feedRepository.findById(feedId).orElse(null);
				if (feed != null) {
					if (feed.getUser() != null) {
						profileImage = getProfileImageFromUser(feed.getUser());
					} else {
						// 업로더가 탈퇴한 경우 uploaderName을 null로 설정
						if (notification.getData().getAdditionalData() != null) {
							notification.getData().getAdditionalData().put("uploaderName", null);
						}
					}
					if (feed.getSelfieImage() != null) {
						selfieImage = s3Service.getImageUrl(feed.getSelfieImage());
					}
				}
			}
		}

		return NotificationResponse.from(notification, profileImage, selfieImage);
	}

	private String getProfileImageBySenderId(Notification notification) {
		if (notification.getData().getAdditionalData() == null) {
			return getDefaultProfileImageUrlService.get();
		}

		Object senderId = notification.getData().getAdditionalData().get("senderId");
		if (senderId != null) {
			try {
				Long userId = Long.valueOf(senderId.toString());
				User user = userRepository.findById(userId).orElse(null);
				if (user != null) {
					return getProfileImageFromUser(user);
				}
			} catch (NumberFormatException e) {
				log.warn("Failed to parse senderId from notification data: {}", senderId);
			}
		}

		return getDefaultProfileImageUrlService.get();
	}

	private String getProfileImageFromUser(User user) {
		if (user == null || user.getProfileImageUrl() == null) {
			return getDefaultProfileImageUrlService.get();
		}

		return s3Service.getImageUrl(user.getProfileImageUrl());
	}

	private Long getFeedIdFromNotification(Notification notification) {
		if (notification.getData().getAdditionalData() == null) {
			return null;
		}

		Object feedId = notification.getData().getAdditionalData().get("feedId");
		if (feedId != null) {
			try {
				return Long.valueOf(feedId.toString());
			} catch (NumberFormatException e) {
				log.warn("Failed to parse feedId from notification data: {}", feedId);
				return null;
			}
		}

		return null;
	}
}
