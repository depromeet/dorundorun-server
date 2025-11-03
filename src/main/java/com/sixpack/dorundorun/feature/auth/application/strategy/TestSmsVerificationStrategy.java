package com.sixpack.dorundorun.feature.auth.application.strategy;

import org.springframework.stereotype.Component;

/**
 * 테스트 환경용 SMS 인증 전략
 * 고정된 인증 코드 사용 및 발송 횟수 제한 없음
 *
 * TODO: 출시 전 이 클래스 전체 삭제
 */
@Component
public class TestSmsVerificationStrategy implements SmsVerificationStrategy {

	private static final String TEST_VERIFICATION_CODE = "000000";

	@Override
	public String generateVerificationCode() {
		return TEST_VERIFICATION_CODE;
	}

	@Override
	public void checkDailySendLimit(String phoneNumber) {
	}

	@Override
	public void incrementDailySendCount(String phoneNumber) {
	}

	@Override
	public boolean supports(String phoneNumber) {
		String normalized = phoneNumber.replaceAll("-", "");
		return TestPhoneNumbers.TEST_NUMBER.equals(normalized);
	}

	@Override
	public String getStrategyName() {
		return "Test";
	}
}
