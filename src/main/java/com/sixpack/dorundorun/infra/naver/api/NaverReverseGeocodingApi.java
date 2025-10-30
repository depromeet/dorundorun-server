package com.sixpack.dorundorun.infra.naver.api;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.sixpack.dorundorun.infra.naver.dto.response.ReverseGeocodingResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverReverseGeocodingApi {

	private final WebClient naverMapsWebClient;

	private static final String REVERSE_GEOCODING_PATH = "/map-reversegeocode/v2/gc";

	public Mono<ReverseGeocodingResponse> reverseGeocode(double latitude, double longitude) {
		String coords = String.format("%f,%f", longitude, latitude);

		return naverMapsWebClient
			.get()
			.uri(uriBuilder -> uriBuilder
				.path(REVERSE_GEOCODING_PATH)
				.queryParam("coords", coords)
				.queryParam("orders", "addr")
				.queryParam("output", "json")
				.build()
			)
			.retrieve()
			.bodyToMono(ReverseGeocodingResponse.class);
	}
}
