package com.example.team6server.global.config.slack;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "slack")
public record SlackProperties(
		boolean enabled,
		String webhookUrl
) {
}
