package com.sixpack.dorundorun.infra.sms.solapi;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Solapi 설정 정보
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sms.solapi")
public class SolapiProperties {

	/**
	 * Solapi API Key
	 */
	private String apiKey;

	/**
	 * Solapi API Secret
	 */
	private String apiSecret;

	/**
	 * 발신번호
	 */
	private String fromNumber;
}
