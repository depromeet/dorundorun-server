package com.sixpack.dorundorun.infra.sms.mock;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.sixpack.dorundorun.global.utils.PhoneNumberMaskUtil;
import com.sixpack.dorundorun.infra.sms.SmsProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * 개발/테스트용 Mock SMS Provider
 * 실제 SMS를 발송하지 않고 로그로만 출력
 * 설정: sms.provider=mock 일 때 활성화 (기본값)
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "mock", matchIfMissing = true)
public class MockSmsProvider implements SmsProvider {

	@Override
	public void sendMessage(String phoneNumber, String message) {
		log.info("=".repeat(80));
		log.info("[MOCK SMS] 📱 SMS would be sent to: {}", PhoneNumberMaskUtil.mask(phoneNumber));
		log.info("[MOCK SMS] 💬 Message: {}", message);
		log.info("=".repeat(80));
	}

	@Override
	public String getProviderName() {
		return "Mock (Development)";
	}
}
