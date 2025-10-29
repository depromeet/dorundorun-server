package com.sixpack.dorundorun.feature.notification.event;

import java.util.Map;

import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;
import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEventType;

import lombok.Builder;

@Builder
public record PushNotificationRequestedEvent(
	Long recipientUserId,
	String notificationType,
	String relatedId,
	Map<String, Object> metadata
) implements RedisStreamEvent {

	public static final String TYPE = RedisStreamEventType.PUSH_NOTIFICATION_REQUESTED;

	@Override
	public String type() {
		return TYPE;
	}
}
