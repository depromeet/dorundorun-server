package com.sixpack.dorundorun.feature.notification.application;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.notification.exception.NotificationErrorCode;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.application.InvalidateDeviceTokenService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.exception.CustomException;
import com.sixpack.dorundorun.infra.firebase.FcmErrorCode;
import com.sixpack.dorundorun.infra.firebase.FcmMessage;
import com.sixpack.dorundorun.infra.firebase.FcmMulticastResult;
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
	private final InvalidateDeviceTokenService invalidateDeviceTokenService;

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

		} catch (CustomException e) {
			// 죽은 토큰은 재시도해도 절대 성공하지 않으므로, 재시도 큐에 남기지 않고
			// 토큰을 무효화한 뒤 정상 종료시켜 PEL/DLQ가 영구 실패 메시지로 오염되는 것을 막는다.
			if (e.getErrorCode() == FcmErrorCode.FCM_TOKEN_UNREGISTERED) {
				log.warn("Stale device token detected, invalidating: recipientId={}", event.recipientUserId());
				invalidateDeviceTokenService.invalidate(event.recipientUserId());
				return null;
			}

			log.error("Failed to send FCM message: recipientId={}, type={}",
				event.recipientUserId(), event.notificationType(), e);
			throw NotificationErrorCode.FAILED_TO_SEND_NOTIFICATION.format(event.notificationType());

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

		// 각 수신자의 Device Token 수집 및 검증.
		// distinct 토큰 -> userId 매핑을 같이 유지해야 멀티캐스트 응답에서 어느 유저의 토큰이
		// 죽었는지 되짚어 무효화할 수 있다.
		Map<String, Long> tokenToUserId = new java.util.LinkedHashMap<>();
		recipientUserIds.forEach(userId -> {
			try {
				User user = findUserByIdService.find(userId);
				String token = user.getDeviceToken();
				if (token != null && !token.isEmpty() && fcmService.isValidToken(token)) {
					tokenToUserId.putIfAbsent(token, userId);
				}
			} catch (Exception e) {
				log.warn("Failed to get device token for user: {}", userId, e);
			}
		});

		if (tokenToUserId.isEmpty()) {
			log.warn("No valid device tokens found for {} recipients", recipientUserIds.size());
			return java.util.Collections.emptyList();
		}

		java.util.List<String> deviceTokens = new java.util.ArrayList<>(tokenToUserId.keySet());

		FcmMessage fcmMessage = buildFcmMessage(
			"",
			title,
			message,
			event,
			deepLink
		);

		try {
			FcmMulticastResult result = fcmService.sendMulticastMessage(fcmMessage, deviceTokens);
			log.info("Multicast FCM messages sent: count={}, type={}",
				result.successMessageIds().size(), event.notificationType());

			result.unregisteredTokens().forEach(token -> {
				Long userId = tokenToUserId.get(token);
				if (userId != null) {
					log.warn("Stale device token detected in multicast, invalidating: recipientId={}", userId);
					invalidateDeviceTokenService.invalidate(userId);
				}
			});

			return result.successMessageIds();

		} catch (Exception e) {
			log.error("Failed to send multicast FCM messages: type={}", event.notificationType(), e);
			throw NotificationErrorCode.FAILED_TO_SEND_NOTIFICATION.format(event.notificationType());
		}
	}
}
