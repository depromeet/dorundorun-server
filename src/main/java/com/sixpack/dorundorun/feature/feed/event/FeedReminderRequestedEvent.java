package com.sixpack.dorundorun.feature.feed.event;

import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;
import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEventType;

import lombok.Builder;

@Builder
public record FeedReminderRequestedEvent(
	Long userId,
	Long runSessionId
) implements RedisStreamEvent {

	public static final String TYPE = RedisStreamEventType.FEED_REMINDER_NOTIFICATION_REQUESTED;

	@Override
	public String type() {
		return TYPE;
	}
}
