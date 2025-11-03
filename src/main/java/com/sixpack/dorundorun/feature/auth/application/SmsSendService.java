package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.auth.application.strategy.SmsVerificationStrategy;
import com.sixpack.dorundorun.feature.auth.application.strategy.SmsVerificationStrategyFactory;
import com.sixpack.dorundorun.feature.auth.dto.request.SmsSendRequest;
import com.sixpack.dorundorun.infra.redis.sms.SmsVerificationCodeManager;
import com.sixpack.dorundorun.infra.sms.SmsProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SMS 인증 코드 발송 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsSendService {

	private final SmsProvider smsProvider;
	private final SmsVerificationCodeManager codeManager;
	private final SmsVerificationStrategyFactory strategyFactory;

	/**
	 * SMS 인증 코드 발송
	 *
	 * @param request SMS 발송 요청 (전화번호 포함)
	 */
	public void sendVerificationCode(SmsSendRequest request) {
		String phoneNumber = request.phoneNumber();

		// 1. 전화번호에 맞는 전략 선택
		SmsVerificationStrategy strategy = strategyFactory.getStrategy(phoneNumber);

		// 2. 하루 발송 횟수 확인
		strategy.checkDailySendLimit(phoneNumber);

		// 3. 인증 코드 생성
		String verificationCode = strategy.generateVerificationCode();

		// 4. Redis에 저장 (3분 TTL)
		codeManager.saveCode(phoneNumber, verificationCode);

		// 5. SMS 발송
		String message = String.format("[두런두런] 인증번호는 [%s]입니다.", verificationCode);
		smsProvider.sendMessage(phoneNumber, message);

		// 6. 하루 발송 횟수 증가
		strategy.incrementDailySendCount(phoneNumber);

		log.info("SMS verification code sent using {} with {} strategy",
			smsProvider.getProviderName(), strategy.getStrategyName());
	}
}
