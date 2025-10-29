package com.sixpack.dorundorun.feature.notification.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.notification.dao.NotificationJpaRepository;
import com.sixpack.dorundorun.feature.notification.domain.Notification;
import com.sixpack.dorundorun.feature.notification.dto.response.NotificationResponse;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsService {

	private final NotificationJpaRepository notificationRepository;

	@Transactional(readOnly = true)
	public Page<NotificationResponse> getNotifications(User user, Pageable pageable) {
		log.debug("Fetching notifications for user: {}", user.getId());

		Page<Notification> notifications = notificationRepository.findByUserDeviceTokenAndDeletedAtIsNull(
			user.getDeviceToken(),
			pageable
		);

		return notifications.map(NotificationResponse::from);
	}
}
