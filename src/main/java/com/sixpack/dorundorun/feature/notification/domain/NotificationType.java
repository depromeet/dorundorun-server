package com.sixpack.dorundorun.feature.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

	// 즉시 처리 알림
	CHEER_FRIEND("깨우기"),
	FEED_UPLOADED("친구의 피드 업로드"),
	FEED_REACTION("피드 리액션"),

	// 스케줄 알림
	FEED_REMINDER("피드 업로드 독촉"),
	RUNNING_PROGRESS_REMINDER("러닝 진행 독촉"),
	NEW_USER_RUNNING_REMINDER("신규 가입 러닝 독촉"),
	NEW_USER_FRIEND_REMINDER("신규 가입 친구추가 독촉");

	private final String description;
}
