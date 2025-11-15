package com.sixpack.dorundorun.infra.firebase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.sixpack.dorundorun.global.config.firebase.FirebaseProperties;
import com.sixpack.dorundorun.global.exception.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "firebase.fcm.enabled", havingValue = "true")
public class FcmServiceImpl implements FcmService {

	private final FirebaseMessaging firebaseMessaging;
	private final FirebaseProperties firebaseProperties;

	@Override
	public String sendMessage(FcmMessage message) {
		if (!isEnabled()) {
			log.warn("FCM service is disabled");
			throw new CustomException(FcmErrorCode.FCM_DISABLED);
		}

		validateDeviceToken(message.deviceToken());
		validateMessage(message);

		try {
			Message fcmMessage = buildMessage(message);
			String messageId = firebaseMessaging.send(fcmMessage);
			log.info("Successfully sent FCM message (messageId: {})", messageId);
			return messageId;

		} catch (Exception e) {
			log.error("Failed to send FCM message, error: {}", e.getMessage(), e);
			throw new CustomException(FcmErrorCode.FCM_SEND_FAILED);
		}
	}

	@Override
	public List<String> sendMulticastMessage(FcmMessage message, List<String> deviceTokens) {
		if (!isEnabled()) {
			throw new CustomException(FcmErrorCode.FCM_DISABLED);
		}

		if (deviceTokens == null || deviceTokens.isEmpty()) {
			return new ArrayList<>();
		}

		List<String> uniqueTokens = deviceTokens.stream()
			.distinct()
			.filter(this::isValidToken)
			.collect(Collectors.toList());

		if (uniqueTokens.isEmpty()) {
			return new ArrayList<>();
		}

		validateMessage(message);

		try {
			MulticastMessage multicastMessage = buildMulticastMessage(message, uniqueTokens);
			var response = firebaseMessaging.sendEachForMulticast(multicastMessage);

			List<String> successMessageIds = new ArrayList<>();
			List<String> failedTokens = new ArrayList<>();

			response.getResponses().forEach(sendResponse -> {
				if (sendResponse.isSuccessful()) {
					successMessageIds.add(sendResponse.getMessageId());
				} else {
					failedTokens.add(sendResponse.getException().getMessage());
				}
			});

			log.info("Multicast message sent. Success: {}, Failed: {}",
				successMessageIds.size(), failedTokens.size());

			if (!failedTokens.isEmpty()) {
				log.warn("Some tokens failed: {}", failedTokens);
			}

			return successMessageIds;

		} catch (Exception e) {
			log.error("Failed to send multicast FCM message, error: {}", e.getMessage(), e);
			throw new CustomException(FcmErrorCode.FCM_SEND_FAILED);
		}
	}

	@Override
	public boolean isEnabled() {
		return firebaseProperties.fcm().enabled();
	}

	@Override
	public boolean isValidToken(String deviceToken) {
		return deviceToken != null && !deviceToken.trim().isEmpty();
	}

	private Message buildMessage(FcmMessage message) {
		Message.Builder builder = Message.builder()
			.setToken(message.deviceToken());

		Notification notification = Notification.builder()
			.setTitle(message.title())
			.setBody(message.body())
			.build();

		builder.setNotification(notification);

		if (message.data() != null) {
			builder.putData("notificationType", message.data().notificationType());
			builder.putData("relatedId", message.data().relatedId());

			if (message.data().metadata() != null && !message.data().metadata().isEmpty()) {
				builder.putAllData(message.data().metadata());
			}
		}

		// 이미지 URL 설정 (data 필드에 포함)
		if (message.imageUrl() != null && !message.imageUrl().isEmpty()) {
			builder.putData("imageUrl", message.imageUrl());
		}

		// Deep Link 설정
		if (message.deepLink() != null && !message.deepLink().isEmpty()) {
			builder.putData("deepLink", message.deepLink());
		}

		return builder.build();
	}

	private MulticastMessage buildMulticastMessage(FcmMessage message, List<String> deviceTokens) {
		MulticastMessage.Builder builder = MulticastMessage.builder()
			.addAllTokens(deviceTokens);

		Notification notification = Notification.builder()
			.setTitle(message.title())
			.setBody(message.body())
			.build();

		builder.setNotification(notification);

		if (message.data() != null) {
			builder.putData("notificationType", message.data().notificationType());
			builder.putData("relatedId", message.data().relatedId());

			if (message.data().metadata() != null && !message.data().metadata().isEmpty()) {
				builder.putAllData(message.data().metadata());
			}
		}

		if (message.imageUrl() != null && !message.imageUrl().isEmpty()) {
			builder.putData("imageUrl", message.imageUrl());
		}

		if (message.deepLink() != null && !message.deepLink().isEmpty()) {
			builder.putData("deepLink", message.deepLink());
		}

		return builder.build();
	}

	private void validateDeviceToken(String deviceToken) {
		if (deviceToken == null || deviceToken.trim().isEmpty()) {
			throw new CustomException(FcmErrorCode.INVALID_DEVICE_TOKEN);
		}

		if (!isValidToken(deviceToken)) {
			throw new CustomException(FcmErrorCode.INVALID_DEVICE_TOKEN);
		}
	}

	private void validateMessage(FcmMessage message) {
		if (message.title() == null || message.title().trim().isEmpty()) {
			throw new CustomException(FcmErrorCode.INVALID_NOTIFICATION_DATA);
		}

		if (message.body() == null || message.body().trim().isEmpty()) {
			throw new CustomException(FcmErrorCode.INVALID_NOTIFICATION_DATA);
		}
	}
}
