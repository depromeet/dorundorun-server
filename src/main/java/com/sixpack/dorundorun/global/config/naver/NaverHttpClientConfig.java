package com.sixpack.dorundorun.global.config.naver;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executor;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({NaverMapsProperties.class, ReverseGeocodingProperties.class})
public class NaverHttpClientConfig {

	@Bean
	public RestClient naverMapsRestClient(NaverMapsProperties naverMapsProperties) {
		HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(5))
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

	@Bean
	public Executor reverseGeocodingExecutor(ReverseGeocodingProperties properties) {
		int maxConcurrent = properties.api().maxConcurrentRequests();
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(maxConcurrent);
		executor.setMaxPoolSize(maxConcurrent);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("reverse-geocoding-");
		executor.setWaitForTasksToCompleteOnShutdown(true);
		executor.setAwaitTerminationSeconds(30);
		executor.initialize();
		return executor;
	}
}
