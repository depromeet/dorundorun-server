package com.sixpack.dorundorun.infra.firebase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import io.micrometer.core.instrument.MeterRegistry;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
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
	private final MeterRegistry meterRegistry;

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
			meterRegistry.counter("fcm.send", "result", "success").increment();
			return messageId;

		} catch (FirebaseMessagingException e) {
			meterRegistry.counter("fcm.send", "result", "failure").increment();

			// 토큰이 영구적으로 무효화된 경우(앱 삭제, 토큰 회전 등)는 재시도해도 절대 성공하지 않는다.
			// 일반 전송 실패와 구분해서 호출부가 재시도 대신 토큰을 정리하도록 알린다.
			if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
				log.warn("FCM token unregistered, error: {}", e.getMessage());
				throw new CustomException(FcmErrorCode.FCM_TOKEN_UNREGISTERED);
			}

			log.error("Failed to send FCM message, error: {}", e.getMessage(), e);
			throw new CustomException(FcmErrorCode.FCM_SEND_FAILED);

		} catch (Exception e) {
			log.error("Failed to send FCM message, error: {}", e.getMessage(), e);
			meterRegistry.counter("fcm.send", "result", "failure").increment();
			throw new CustomException(FcmErrorCode.FCM_SEND_FAILED);
		}
	}

	@Override
	public FcmMulticastResult sendMulticastMessage(FcmMessage message, List<String> deviceTokens) {
		if (!isEnabled()) {
			throw new CustomException(FcmErrorCode.FCM_DISABLED);
		}

		if (deviceTokens == null || deviceTokens.isEmpty()) {
			return new FcmMulticastResult(new ArrayList<>(), new ArrayList<>());
		}

		List<String> uniqueTokens = deviceTokens.stream()
			.distinct()
			.filter(this::isValidToken)
			.collect(Collectors.toList());

		if (uniqueTokens.isEmpty()) {
			return new FcmMulticastResult(new ArrayList<>(), new ArrayList<>());
		}

		validateMessage(message);

		try {
			MulticastMessage multicastMessage = buildMulticastMessage(message, uniqueTokens);
			var response = firebaseMessaging.sendEachForMulticast(multicastMessage);

			List<String> successMessageIds = new ArrayList<>();
			List<String> failedTokens = new ArrayList<>();
			List<String> unregisteredTokens = new ArrayList<>();

			var sendResponses = response.getResponses();
			for (int i = 0; i < sendResponses.size(); i++) {
				var sendResponse = sendResponses.get(i);
				String token = uniqueTokens.get(i);

				if (sendResponse.isSuccessful()) {
					successMessageIds.add(sendResponse.getMessageId());
					continue;
				}

				FirebaseMessagingException exception = sendResponse.getException();
				failedTokens.add(exception.getMessage());

				// 토큰이 영구적으로 무효화된 경우 - 재시도 대상이 아니라 호출부가 정리하도록 별도로 모은다.
				if (exception.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
					unregisteredTokens.add(token);
				}
			}

			log.info("Multicast message sent. Success: {}, Failed: {}",
				successMessageIds.size(), failedTokens.size());

			if (!failedTokens.isEmpty()) {
				log.warn("Some tokens failed: {}", failedTokens);
			}

			meterRegistry.counter("fcm.send", "result", "success").increment(successMessageIds.size());
			meterRegistry.counter("fcm.send", "result", "failure").increment(failedTokens.size());

			return new FcmMulticastResult(successMessageIds, unregisteredTokens);

		} catch (Exception e) {
			log.error("Failed to send multicast FCM message, error: {}", e.getMessage(), e);
			meterRegistry.counter("fcm.send", "result", "failure").increment(uniqueTokens.size());
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
			if (message.data().notificationType() != null) {
				builder.putData("notificationType", message.data().notificationType());
			}

			if (message.data().relatedId() != null) {
				builder.putData("relatedId", message.data().relatedId());
			}

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
			if (message.data().notificationType() != null) {
				builder.putData("notificationType", message.data().notificationType());
			}

			if (message.data().relatedId() != null) {
				builder.putData("relatedId", message.data().relatedId());
			}

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
