package com.sixpack.dorundorun.global.config.firebase;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "firebase")
public record FirebaseProperties(
	@NestedConfigurationProperty
	Config config,
	@NestedConfigurationProperty
	Fcm fcm
) {
	public record Config(
		String serviceAccountPath,
		String projectId
	) {
	}

	public record Fcm(
		boolean enabled,
		int maxRetryCount,
		long retryDelayMs // 재시도 간격(ms), 2000 -> 2초
	) {
	}
}
