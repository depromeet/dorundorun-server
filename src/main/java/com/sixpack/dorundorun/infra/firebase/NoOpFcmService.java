package com.sixpack.dorundorun.infra.firebase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * FCM이 비활성화되었을 때 사용되는 No-Op 구현체
 * 실제로 FCM으로 메시지를 보내지 않고, 로그만 남깁니다.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "firebase.fcm.enabled", havingValue = "false")
public class NoOpFcmService implements FcmService {

	@Override
	public String sendMessage(FcmMessage message) {
		log.warn("FCM is disabled. Message would have been sent to token: {} (title: {})",
			message.deviceToken(), message.title());
		return "mock-message-id-" + System.nanoTime();
	}

	@Override
	public List<String> sendMulticastMessage(FcmMessage message, List<String> deviceTokens) {
		log.warn("FCM is disabled. Message would have been sent to {} devices", deviceTokens.size());
		List<String> mockIds = new ArrayList<>();
		for (int i = 0; i < deviceTokens.size(); i++) {
			mockIds.add("mock-message-id-" + i + "-" + System.nanoTime());
		}
		return mockIds;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public boolean isValidToken(String deviceToken) {
		return deviceToken != null && !deviceToken.isEmpty();
	}
}
