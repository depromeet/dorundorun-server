package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.stereotype.Service;

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

	/**
	 * SMS 인증 코드 발송
	 *
	 * @param request SMS 발송 요청 (전화번호 포함)
	 */
	public void sendVerificationCode(SmsSendRequest request) {
		String phoneNumber = request.phoneNumber();

		// 1. 하루 발송 횟수 확인 (하루 5회 제한)
		codeManager.checkDailySendLimit(phoneNumber);

		// 2. 6자리 인증 코드 생성
		String verificationCode = codeManager.generateCode();

		// 3. Redis에 저장 (3분 TTL)
		codeManager.saveCode(phoneNumber, verificationCode);

		// 4. SMS 발송
		String message = String.format("[두런두런] 인증번호는 [%s]입니다. 3분 이내에 입력해주세요.", verificationCode);
		smsProvider.sendMessage(phoneNumber, message);

		// 5. 하루 발송 횟수 증가
		codeManager.incrementDailySendCount(phoneNumber);

		log.info("SMS verification code sent using {}", smsProvider.getProviderName());
	}
}
