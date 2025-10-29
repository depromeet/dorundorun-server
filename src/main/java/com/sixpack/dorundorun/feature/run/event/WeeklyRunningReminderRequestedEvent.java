package com.sixpack.dorundorun.feature.run.event;

import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;
import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEventType;

import lombok.Builder;

@Builder
public record WeeklyRunningReminderRequestedEvent(
	Long userId,
	int daysSinceLastRun
) implements RedisStreamEvent {

	public static final String TYPE = RedisStreamEventType.WEEKLY_RUNNING_PROGRESS_REMINDER_NOTIFICATION_REQUESTED;

	@Override
	public String type() {
		return TYPE;
	}
}
