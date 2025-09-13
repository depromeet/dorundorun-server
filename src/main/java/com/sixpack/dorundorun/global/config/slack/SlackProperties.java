package com.sixpack.dorundorun.global.config.slack;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "slack")
public record SlackProperties(
		boolean enabled,
		String webhookUrl
) {
}
