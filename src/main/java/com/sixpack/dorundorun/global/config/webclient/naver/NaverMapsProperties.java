package com.sixpack.dorundorun.global.config.webclient.naver;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "naver.maps")
public record NaverMapsProperties(
	String apiKey,
	String apiSecret,
	String baseUrl,
	long timeoutSeconds
) {
}
