package com.sixpack.dorundorun.feature.notification.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.notification.dao.NotificationJpaRepository;
import com.sixpack.dorundorun.feature.notification.domain.Notification;
import com.sixpack.dorundorun.feature.notification.exception.NotificationErrorCode;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkNotificationAsReadService {

	private final NotificationJpaRepository notificationRepository;

	@Transactional
	public void markAsRead(Long notificationId, User user) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> NotificationErrorCode.NOT_FOUND_NOTIFICATION.format(notificationId));

		if (!notification.getUserDeviceToken().equals(user.getDeviceToken())) {
			throw NotificationErrorCode.UNAUTHORIZED_NOTIFICATION_ACCESS.format(notificationId);
		}

		if (!notification.getIsRead()) {
			notificationRepository.markAsReadById(notificationId);
			log.info("Notification marked as read: id={}, userId={}", notificationId, user.getId());
		} else {
			log.debug("Notification already read: id={}", notificationId);
		}
	}
}
