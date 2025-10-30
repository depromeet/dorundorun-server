package com.sixpack.dorundorun.infra.sms;

/**
 * SMS 발송 벤더 추상화 인터페이스
 * 다양한 SMS 벤더(CoolSMS, Solapi 등)를 갈아끼울 수 있도록 설계
 */
public interface SmsProvider {

	void sendMessage(String phoneNumber, String message);

	String getProviderName();
}
