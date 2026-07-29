package com.sixpack.dorundorun.global.config.naver;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({NaverMapsProperties.class, ReverseGeocodingProperties.class})
public class NaverHttpClientConfig {

	@Bean
	public RestClient naverMapsRestClient(NaverMapsProperties naverMapsProperties) {
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.version(HttpClient.Version.HTTP_2)
				.build();

		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
		factory.setReadTimeout(Duration.ofSeconds(naverMapsProperties.timeoutSeconds()));

		return RestClient.builder()
				.baseUrl(naverMapsProperties.baseUrl())
				.requestFactory(factory)
				.defaultHeader("x-ncp-apigw-api-key-id", naverMapsProperties.apiKey())
				.defaultHeader("x-ncp-apigw-api-key", naverMapsProperties.apiSecret())
				.defaultHeader("Content-Type", "application/json")
				.build();
	}
}
