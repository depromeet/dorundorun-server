package com.sixpack.dorundorun.infra.slack;

import com.slack.api.webhook.WebhookResponse;

public interface SlackNotifier {

	WebhookResponse send(String text);
}
