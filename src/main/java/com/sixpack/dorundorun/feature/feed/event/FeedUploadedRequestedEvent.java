package com.sixpack.dorundorun.feature.feed.event;

import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;
import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEventType;

import lombok.Builder;

@Builder
public record FeedUploadedRequestedEvent(
	Long userId,
	Long feedId
) implements RedisStreamEvent {

	public static final String TYPE = RedisStreamEventType.FEED_UPLOADED_NOTIFICATION_REQUESTED;

	@Override
	public String type() {
		return TYPE;
	}
}
