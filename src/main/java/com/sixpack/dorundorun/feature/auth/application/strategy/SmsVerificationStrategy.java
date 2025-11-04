package com.sixpack.dorundorun.feature.auth.application.strategy;

public interface SmsVerificationStrategy {

	String generateVerificationCode();

	void checkDailySendLimit(String phoneNumber);

	void incrementDailySendCount(String phoneNumber);

	void sendSms(String phoneNumber, String message);

	boolean supports(String phoneNumber);

	String getStrategyName();
}
