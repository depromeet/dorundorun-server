package com.sixpack.dorundorun.infra.naver.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReverseGeocodingResponse(
	Status status,
	List<Result> results
) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Status(
		int code,
		String name,
		String message
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Result(
		Region region
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Region(
		@JsonProperty("area1")
		Area area1,

		@JsonProperty("area2")
		Area area2,

		@JsonProperty("area3")
		Area area3
	) {
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Area(
		String name
	) {
	}
}
