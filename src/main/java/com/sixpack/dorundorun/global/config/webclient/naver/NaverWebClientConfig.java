package com.sixpack.dorundorun.global.config.webclient.naver;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
@EnableConfigurationProperties({NaverMapsProperties.class, ReverseGeocodingProperties.class})
public class NaverWebClientConfig {

	@Bean
	public WebClient naverMapsWebClient(
		WebClient.Builder builder,
		NaverMapsProperties naverMapsProperties,
		ObjectMapper objectMapper
	) {
		HttpClient httpClient = HttpClient.create(
				ConnectionProvider.builder("naver-maps-pool")
					.maxConnections(100)
					.maxIdleTime(Duration.ofSeconds(30))
					.build()
			)
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
			.option(ChannelOption.SO_KEEPALIVE, true)
			.responseTimeout(Duration.ofSeconds(naverMapsProperties.timeoutSeconds()))
			.doOnConnected(conn ->
				conn.addHandlerLast(
						new ReadTimeoutHandler((int)naverMapsProperties.timeoutSeconds())
					)
					.addHandlerLast(
						new WriteTimeoutHandler((int)naverMapsProperties.timeoutSeconds())
					)
			);

		return builder
			.baseUrl(naverMapsProperties.baseUrl())
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.defaultHeaders(httpHeaders -> {
				httpHeaders.set("x-ncp-apigw-api-key-id", naverMapsProperties.apiKey());
				httpHeaders.set("x-ncp-apigw-api-key", naverMapsProperties.apiSecret());
				httpHeaders.set("Content-Type", "application/json");
			})
			.build();
	}
}
