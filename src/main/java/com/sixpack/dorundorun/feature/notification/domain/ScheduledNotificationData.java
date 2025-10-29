package com.sixpack.dorundorun.feature.notification.domain;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//  Redis Sorted Set에 저장될 예약 알림 데이터
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledNotificationData {

	// 예약 알림의 고유 ID (UUID)
	private String eventId;

	private String notificationType;

	private Long userId;

	private LocalDateTime scheduledAt;
}
