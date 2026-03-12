package com.sixpack.dorundorun.infra.firebase;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * FCM 레이턴시를 시뮬레이션하는 Stub 구현체 (성능 테스트 전용)
 * firebase.fcm.enabled=stub 으로 활성화
 * Thread.sleep(200ms)로 FCM 평균 응답 지연을 재현
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "firebase.fcm.enabled", havingValue = "stub")
public class FcmServiceStub implements FcmService {

    private static final long FCM_LATENCY_MS = 200;

    @Override
    public String sendMessage(FcmMessage message) {
        try {
            Thread.sleep(FCM_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.debug("FCM stub: message sent to token={}", message.deviceToken());
        return "stub-" + System.nanoTime();
    }

    @Override
    public List<String> sendMulticastMessage(FcmMessage message, List<String> deviceTokens) {
        try {
            Thread.sleep(FCM_LATENCY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < deviceTokens.size(); i++) {
            ids.add("stub-" + i + "-" + System.nanoTime());
        }
        return ids;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isValidToken(String deviceToken) {
        return deviceToken != null && !deviceToken.isEmpty();
    }
}
