package com.sixpack.dorundorun.feature.notification.application;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.notification.dao.NotificationJpaRepository;
import com.sixpack.dorundorun.feature.notification.domain.Notification;
import com.sixpack.dorundorun.feature.notification.domain.NotificationData;
import com.sixpack.dorundorun.feature.notification.domain.NotificationType;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveNotificationService {

	private final NotificationJpaRepository notificationRepository;
	private final FindUserByIdService findUserByIdService;

	public Notification save(
		PushNotificationRequestedEvent event,
		String title,
		String message,
		String deepLink
	) {
		User recipient = findUserByIdService.find(event.recipientUserId());

		// metadata와 함께 additionalData 생성
		Map<String, Object> additionalData = new HashMap<>();
		additionalData.put("notificationType", event.notificationType());
		additionalData.put("relatedId", event.relatedId());
		if (event.metadata() != null) {
			additionalData.putAll(event.metadata());
		}

		NotificationData notificationData = NotificationData.builder()
			.title(title)
			.message(message)
			.additionalData(additionalData)
			.build();

		// 알림 타입을 문자열에서 NotificationType enum으로 변환
		NotificationType notificationType = convertToNotificationType(event.notificationType());

		Notification notification = Notification.builder()
			.userDeviceToken(recipient.getDeviceToken())
			.recipientUserId(event.recipientUserId())
			.type(notificationType)
			.data(notificationData)
			.isRead(false)
			.deepLink(deepLink)
			.build();

		Notification saved = notificationRepository.save(notification);
		log.debug("Notification saved: id={}, recipientId={}, type={}, deepLink={}",
			saved.getId(), event.recipientUserId(), event.notificationType(), deepLink);

		return saved;
	}

	// PushNotificationRequestedEvent의 notificationType 문자열을 NotificationType enum으로 변환
	private NotificationType convertToNotificationType(String notificationTypeString) {
		return switch (notificationTypeString) {
			case "CHEER_FRIEND" -> NotificationType.CHEER_FRIEND;
			case "FEED_UPLOADED" -> NotificationType.FEED_UPLOADED;
			case "FEED_REACTION" -> NotificationType.FEED_REACTION;
			case "FEED_REMINDER" -> NotificationType.FEED_REMINDER;
			case "RUNNING_PROGRESS_REMINDER" -> NotificationType.RUNNING_PROGRESS_REMINDER;
			case "NEW_USER_RUNNING_REMINDER" -> NotificationType.NEW_USER_RUNNING_REMINDER;
			case "NEW_USER_FRIEND_REMINDER" -> NotificationType.NEW_USER_FRIEND_REMINDER;
			default -> NotificationType.CHEER_FRIEND; // 기본값
		};
	}
}
