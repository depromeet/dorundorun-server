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
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.utils.S3ImageUrlUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsService {

	private final NotificationJpaRepository notificationRepository;
	private final FeedJpaRepository feedRepository;

	@Transactional(readOnly = true)
	public Page<NotificationResponse> getNotifications(User user, Pageable pageable) {
		log.debug("Fetching notifications for user: {}", user.getId());

		Page<Notification> notifications = notificationRepository.findByUserDeviceTokenAndDeletedAtIsNull(
			user.getDeviceToken(),
			pageable
		);

		return notifications.map(notification -> buildNotificationResponse(notification));
	}

	private NotificationResponse buildNotificationResponse(Notification notification) {
		String profileImage = "/api/images/defaultProfileImage.jpg";
		String selfieImage = null;

		// FEED_UPLOADED 타입일 때만 feed의 selfieImage 조회
		if (notification.getType() == NotificationType.FEED_UPLOADED) {
			Long feedId = getFeedIdFromNotification(notification);
			if (feedId != null) {
				Feed feed = feedRepository.findById(feedId).orElse(null);
				if (feed != null && feed.getSelfieImage() != null) {
					selfieImage = S3ImageUrlUtil.getPresignedImageUrl(feed.getSelfieImage());
				}
			}
		}

		return NotificationResponse.from(notification, profileImage, selfieImage);
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
