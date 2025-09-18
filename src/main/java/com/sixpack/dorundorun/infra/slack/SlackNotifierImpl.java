package com.sixpack.dorundorun.infra.slack;

import com.sixpack.dorundorun.global.config.slack.SlackProperties;
import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import com.slack.api.webhook.WebhookResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackNotifierImpl implements SlackNotifier {

	private final SlackProperties props;

	private final Slack slackClient = Slack.getInstance();

	@Override
	public WebhookResponse send(String paramText) {
		Payload payload = Payload.builder().text(paramText).build();
		WebhookResponse response;
		try {
			response = slackClient.send(props.webhookUrl(), payload);
			return response;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}