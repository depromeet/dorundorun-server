package com.sixpack.dorundorun.feature.auth.application.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * SMS 인증 전략 팩토리
 * 전화번호에 따라 적절한 전략(운영/테스트)을 선택
 */
@Component
@RequiredArgsConstructor
public class SmsVerificationStrategyFactory {

	private final List<SmsVerificationStrategy> strategies;

	public SmsVerificationStrategy getStrategy(String phoneNumber) {
		return strategies.stream()
			.filter(strategy -> strategy.supports(phoneNumber))
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No strategy found for phone number: " + phoneNumber));
	}
}
