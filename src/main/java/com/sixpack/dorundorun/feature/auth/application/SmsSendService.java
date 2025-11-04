package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.auth.application.strategy.SmsVerificationStrategy;
import com.sixpack.dorundorun.feature.auth.application.strategy.SmsVerificationStrategyFactory;
import com.sixpack.dorundorun.feature.auth.dto.request.SmsSendRequest;
import com.sixpack.dorundorun.global.utils.PhoneNumberNormalizationUtil;
import com.sixpack.dorundorun.infra.redis.sms.SmsVerificationCodeManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SMS 인증 코드 발송 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsSendService {

	private final PhoneNumberNormalizationUtil phoneNumberNormalizationUtil;
	private final SmsVerificationCodeManager codeManager;
	private final SmsVerificationStrategyFactory strategyFactory;

	/**
	 * SMS 인증 코드 발송
	 *
	 * @param request SMS 발송 요청 (전화번호 포함)
	 */
	public void sendVerificationCode(SmsSendRequest request) {
		String phoneNumber = phoneNumberNormalizationUtil.normalize(request.phoneNumber());

		SmsVerificationStrategy strategy = strategyFactory.getStrategy(phoneNumber);

		strategy.checkDailySendLimit(phoneNumber);

		String verificationCode = strategy.generateVerificationCode();

		codeManager.saveCode(phoneNumber, verificationCode);

		String message = String.format("[두런두런] 인증번호는 [%s]입니다.", verificationCode);
		strategy.sendSms(phoneNumber, message);

		strategy.incrementDailySendCount(phoneNumber);

		log.info("SMS verification code sent with {} strategy", strategy.getStrategyName());
	}
}
