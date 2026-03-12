package com.sixpack.dorundorun.infra.naver.api;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.sixpack.dorundorun.infra.naver.dto.response.ReverseGeocodingResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NaverReverseGeocodingApi {

	private final RestClient naverMapsRestClient;

	private static final String REVERSE_GEOCODING_PATH = "/map-reversegeocode/v2/gc";

	public ReverseGeocodingResponse reverseGeocode(double latitude, double longitude) {
		String coords = String.format("%f,%f", longitude, latitude);

		return naverMapsRestClient.get()
			.uri(uriBuilder -> uriBuilder
				.path(REVERSE_GEOCODING_PATH)
				.queryParam("coords", coords)
				.queryParam("orders", "addr")
				.queryParam("output", "json")
				.build()
			)
			.retrieve()
			.body(ReverseGeocodingResponse.class);
	}
}
