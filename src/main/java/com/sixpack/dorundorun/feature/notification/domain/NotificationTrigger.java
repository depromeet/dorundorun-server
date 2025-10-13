package com.sixpack.dorundorun.feature.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationTrigger {
	TRIGGER("트리거"),
	CONDITIONAL("조건부");

	private final String description;
}
