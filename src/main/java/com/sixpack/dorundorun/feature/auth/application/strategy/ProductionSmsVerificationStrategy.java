package com.sixpack.dorundorun.feature.auth.application.strategy;

import org.springframework.stereotype.Component;

import com.sixpack.dorundorun.infra.redis.sms.SmsVerificationCodeManager;

import lombok.RequiredArgsConstructor;

/**
 * 운영 환경용 SMS 인증 전략
 * 실제 랜덤 인증 코드 생성 및 발송 횟수 제한 적용
 */
@Component
@RequiredArgsConstructor
public class ProductionSmsVerificationStrategy implements SmsVerificationStrategy {

	private final SmsVerificationCodeManager codeManager;

	@Override
	public String generateVerificationCode() {
		return codeManager.generateCode();
	}

	@Override
	public void checkDailySendLimit(String phoneNumber) {
		codeManager.checkDailySendLimit(phoneNumber);
	}

	@Override
	public void incrementDailySendCount(String phoneNumber) {
		codeManager.incrementDailySendCount(phoneNumber);
	}

	@Override
	public boolean supports(String phoneNumber) {
		String normalized = phoneNumber.replaceAll("-", "");
		return !TestPhoneNumbers.TEST_NUMBER.equals(normalized);
	}

	@Override
	public String getStrategyName() {
		return "Production";
	}
}
