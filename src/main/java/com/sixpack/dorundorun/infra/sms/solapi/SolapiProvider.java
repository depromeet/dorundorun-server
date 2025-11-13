package com.sixpack.dorundorun.infra.sms.solapi;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.sixpack.dorundorun.feature.auth.exception.AuthErrorCode;
import com.sixpack.dorundorun.global.exception.CustomException;
import com.sixpack.dorundorun.global.utils.PhoneNumberMaskUtil;
import com.sixpack.dorundorun.infra.sms.SmsProvider;
import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.exception.SolapiEmptyResponseException;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;
import com.solapi.sdk.message.exception.SolapiUnknownException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Solapi를 사용한 SMS 발송 구현체
 * 설정: sms.provider=solapi 일 때 활성화
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "sms.provider", havingValue = "solapi")
public class SolapiProvider implements SmsProvider {

	private final SolapiProperties solapiProperties;
	private DefaultMessageService messageService;

	@PostConstruct
	public void init() {
		this.messageService = SolapiClient.INSTANCE.createInstance(
			solapiProperties.getApiKey(),
			solapiProperties.getApiSecret()
		);
	}

	@Override
	public void sendMessage(String phoneNumber, String messageText) {
		log.info("[Solapi] Sending SMS to {}", PhoneNumberMaskUtil.mask(phoneNumber));

		try {
			// 전화번호 정규화 (하이픈 제거 - Solapi는 01012345678 형식 요구)
			String normalizedTo = phoneNumber.replaceAll("-", "");
			String normalizedFrom = solapiProperties.getFromNumber().replaceAll("-", "");

			// Message 객체 생성
			Message message = new Message();
			message.setFrom(normalizedFrom);
			message.setTo(normalizedTo);
			message.setText(messageText);

			// SMS 발송 (단일 메시지)
			messageService.send(message);

			log.info("[Solapi] SMS sent successfully to {}",
				PhoneNumberMaskUtil.mask(phoneNumber));

		} catch (SolapiMessageNotReceivedException e) {
			// 메시지 수신 실패 (발송은 시도했으나 실패한 메시지 목록 있음)
			log.error("[Solapi] Message not received - failed messages: {}", e.getFailedMessageList(), e);
			throw new CustomException(AuthErrorCode.SMS_SEND_FAILED);
		} catch (SolapiEmptyResponseException e) {
			// 응답이 비어있음
			log.error("[Solapi] Empty response from Solapi API", e);
			throw new CustomException(AuthErrorCode.SMS_SEND_FAILED);
		} catch (SolapiUnknownException e) {
			// 알 수 없는 Solapi 오류
			log.error("[Solapi] Unknown Solapi exception", e);
			throw new CustomException(AuthErrorCode.SMS_SEND_FAILED);
		} catch (CustomException e) {
			// CustomException은 그대로 전파
			throw e;
		} catch (Exception e) {
			// 기타 예상치 못한 오류
			log.error("[Solapi] Unexpected error while sending SMS", e);
			throw new CustomException(AuthErrorCode.SMS_SEND_FAILED);
		}
	}

	@Override
	public String getProviderName() {
		return "Solapi";
	}
}
