package com.sixpack.dorundorun.feature.notification.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.notification.dao.NotificationJpaRepository;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UnreadNotificationCountService {

	private final NotificationJpaRepository notificationRepository;

	@Transactional(readOnly = true)
	public int getUnreadCount(User user) {
		long count = notificationRepository.countUnreadByUserDeviceToken(user.getDeviceToken());
		return (int)count;
	}
}
