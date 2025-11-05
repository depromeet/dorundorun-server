package com.sixpack.dorundorun.feature.notification.domain;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledNotificationData {

	private String eventId;

	private String notificationType;

	private Long userId;

	private LocalDateTime scheduledAt;

	@Builder.Default
	private Map<String, Object> additionalData = new java.util.HashMap<>();
}
