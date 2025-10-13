package com.sixpack.dorundorun.feature.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
	PUSH("푸시 알림"),
	INAPP("인앱 알림");

	private final String description;
}
