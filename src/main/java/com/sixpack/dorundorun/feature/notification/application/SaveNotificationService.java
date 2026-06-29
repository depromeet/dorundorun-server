package com.sixpack.dorundorun.feature.notification.application;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.notification.dao.NotificationJpaRepository;
import com.sixpack.dorundorun.feature.notification.domain.Notification;
import com.sixpack.dorundorun.feature.notification.domain.NotificationData;
import com.sixpack.dorundorun.feature.notification.domain.NotificationType;
import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;

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
		String deduplicationKey = event.idempotencyKey();

		if (deduplicationKey != null) {
			Optional<Notification> existing = notificationRepository.findByDeduplicationKey(deduplicationKey);
			if (existing.isPresent()) {
				log.info("Duplicate notification in DB, returning existing: id={}", existing.get().getId());
				return existing.get();
			}
		}

		User recipient = findUserByIdService.find(event.recipientUserId());

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

		NotificationType notificationType = convertToNotificationType(event.notificationType());

		Notification notification = Notification.builder()
			.userDeviceToken(recipient.getDeviceToken())
			.recipientUserId(event.recipientUserId())
			.type(notificationType)
			.data(notificationData)
			.isRead(false)
			.deepLink(deepLink)
			.deduplicationKey(deduplicationKey)
			.build();

		try {
			Notification saved = notificationRepository.save(notification);
			log.debug("Notification saved: id={}, recipientId={}, type={}, deepLink={}",
				saved.getId(), event.recipientUserId(), event.notificationType(), deepLink);
			return saved;
		} catch (DataIntegrityViolationException e) {
			if (deduplicationKey == null) throw e;
			log.warn("Deduplication key conflict, returning existing: recipientId={}, type={}",
				event.recipientUserId(), event.notificationType());
			return notificationRepository.findByDeduplicationKey(deduplicationKey)
				.orElseThrow(() -> e);
		}
	}

	private NotificationType convertToNotificationType(String notificationTypeString) {
		return switch (notificationTypeString) {
			case "CHEER_FRIEND" -> NotificationType.CHEER_FRIEND;
			case "FEED_UPLOADED" -> NotificationType.FEED_UPLOADED;
			case "FEED_REACTION" -> NotificationType.FEED_REACTION;
			case "FEED_REMINDER" -> NotificationType.FEED_REMINDER;
			case "RUNNING_PROGRESS_REMINDER" -> NotificationType.RUNNING_PROGRESS_REMINDER;
			case "NEW_USER_RUNNING_REMINDER" -> NotificationType.NEW_USER_RUNNING_REMINDER;
			case "NEW_USER_FRIEND_REMINDER" -> NotificationType.NEW_USER_FRIEND_REMINDER;
			default -> throw new IllegalArgumentException("Unknown notification type: " + notificationTypeString);
		};
	}
}
