package com.sixpack.dorundorun.feature.auth.application.strategy;

public interface SmsVerificationStrategy {

	String generateVerificationCode();

	void checkDailySendLimit(String phoneNumber);

	void incrementDailySendCount(String phoneNumber);

	boolean supports(String phoneNumber);

	String getStrategyName();
}
