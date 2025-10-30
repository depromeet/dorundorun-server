package com.sixpack.dorundorun.global.config.webclient.naver;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "reverse-geocoding")
public record ReverseGeocodingProperties(
	@NestedConfigurationProperty
	Cache cache,

	@NestedConfigurationProperty
	Api api
) {

	public record Cache(
		long ttlHours,
		boolean enabled,
		int coordinateDecimalPlaces
	) {
	}

	public record Api(
		int maxConcurrentRequests,
		int retryAttempts,
		long retryDelayMillis,
		String fallbackAddress
	) {
	}
}
