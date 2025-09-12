package com.example.team6server.feature.user.event;

import com.example.team6server.infra.redis.stream.annotation.RedisStreamEventListener;
import com.example.team6server.infra.redis.stream.handler.AbstractRedisStreamEventHandler;
import com.example.team6server.infra.slack.SlackNotifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RedisStreamEventListener
public class UserRegisteredEventListener extends AbstractRedisStreamEventHandler<UserRegisteredEvent> {

	public static final String NEW_USER_WELCOME_MESSAGE = """
			:star2: *새로운 회원 가입 소식!* :star2:
			
			- 이름: %s
			- 이메일: `%s`
			- 사용자 ID: `%d`
			
			:wave: 환영합니다!
			""";
	private final SlackNotifier slackNotifier;

	public UserRegisteredEventListener(ObjectMapper objectMapper, SlackNotifier slackNotifier) {
		super(objectMapper);
		this.slackNotifier = slackNotifier;
	}

	@Override
	public String getEventType() {
		return UserRegisteredEvent.TYPE;
	}

	@Override
	protected Class<UserRegisteredEvent> payloadType() {
		return UserRegisteredEvent.class;
	}

	@Override
	protected void onMessage(UserRegisteredEvent event) {
		log.info("Processing user registration: userId={}, email={}", event.userId(), event.email());

		slackNotifier.send(NEW_USER_WELCOME_MESSAGE.formatted(event.name(), event.email(), event.userId()));

		log.info("User registration processed: userId={}", event.userId());
	}
}
