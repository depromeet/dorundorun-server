package com.sixpack.dorundorun.feature.user.event;

import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;
import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEventType;

import lombok.Builder;

@Builder
public record NewUserFriendReminderRequestedEvent(
	Long userId
) implements RedisStreamEvent {

	public static final String TYPE = RedisStreamEventType.NEW_USER_FRIEND_REMINDER_NOTIFICATION_REQUESTED;

	@Override
	public String type() {
		return TYPE;
	}
}
