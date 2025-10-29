package com.sixpack.dorundorun.feature.notification.application;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.notification.exception.NotificationErrorCode;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.firebase.FcmMessage;
import com.sixpack.dorundorun.infra.firebase.FcmService;
import com.sixpack.dorundorun.feature.notification.event.PushNotificationRequestedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendPushNotificationService {

	private final FcmService fcmService;
	private final FindUserByIdService findUserByIdService;

	// 단일 사용자에게 푸시 알림 발송
	public String send(
		PushNotificationRequestedEvent event,
		String title,
		String message,
		String deepLink
	) {
		if (!fcmService.isEnabled()) {
			log.warn("FCM service is disabled, cannot send notification: recipientId={}",
				event.recipientUserId());
			return null;
		}

		User recipient = findUserByIdService.find(event.recipientUserId());

		if (recipient.getDeviceToken() == null || recipient.getDeviceToken().isEmpty()) {
			log.warn("Device token not found for user: {}", event.recipientUserId());
			return null;
		}

		FcmMessage fcmMessage = buildFcmMessage(
			recipient.getDeviceToken(),
			title,
			message,
			event,
			deepLink
		);

		try {
			String messageId = fcmService.sendMessage(fcmMessage);
			log.info("FCM message sent successfully: recipientId={}, messageId={}, type={}",
				event.recipientUserId(), messageId, event.notificationType());
			return messageId;

		} catch (Exception e) {
			log.error("Failed to send FCM message: recipientId={}, type={}",
				event.recipientUserId(), event.notificationType(), e);
			throw NotificationErrorCode.FAILED_TO_SEND_NOTIFICATION.format(event.notificationType());
		}
	}

	// PushNotificationRequestedEvent를 FCM 메시지 형식으로 변환
	private FcmMessage buildFcmMessage(
		String deviceToken,
		String title,
		String message,
		PushNotificationRequestedEvent event,
		String deepLink
	) {
		Map<String, String> metadataMap = new HashMap<>();
		if (event.metadata() != null) {
			event.metadata().forEach((key, value) -> {
				metadataMap.put(key, String.valueOf(value));
			});
		}

		return new FcmMessage(
			deviceToken,
			title,
			message,
			new FcmMessage.NotificationData(
				event.notificationType(),
				event.relatedId(),
				metadataMap
			),
			null,
			deepLink
		);
	}

	// 여러 사용자에게 푸시 알림 발송
	public java.util.List<String> sendMultiple(
		PushNotificationRequestedEvent event,
		java.util.List<Long> recipientUserIds,
		String title,
		String message,
		String deepLink
	) {
		if (!fcmService.isEnabled()) {
			log.warn("FCM service is disabled, cannot send notifications");
			return java.util.Collections.emptyList();
		}

		// 각 수신자의 Device Token 수집 및 검증
		java.util.List<String> deviceTokens = recipientUserIds.stream()
			.map(userId -> {
				try {
					User user = findUserByIdService.find(userId);
					return user.getDeviceToken();
				} catch (Exception e) {
					log.warn("Failed to get device token for user: {}", userId, e);
					return null;
				}
			})
			.filter(token -> token != null && !token.isEmpty())
			.filter(fcmService::isValidToken)
			.distinct()
			.toList();

		if (deviceTokens.isEmpty()) {
			log.warn("No valid device tokens found for {} recipients", recipientUserIds.size());
			return java.util.Collections.emptyList();
		}

		FcmMessage fcmMessage = buildFcmMessage(
			"",
			title,
			message,
			event,
			deepLink
		);

		try {
			java.util.List<String> messageIds = fcmService.sendMulticastMessage(fcmMessage, deviceTokens);
			log.info("Multicast FCM messages sent: count={}, type={}", messageIds.size(), event.notificationType());
			return messageIds;

		} catch (Exception e) {
			log.error("Failed to send multicast FCM messages: type={}", event.notificationType(), e);
			throw NotificationErrorCode.FAILED_TO_SEND_NOTIFICATION.format(event.notificationType());
		}
	}
}
