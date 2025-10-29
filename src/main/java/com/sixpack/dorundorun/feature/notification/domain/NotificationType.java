package com.sixpack.dorundorun.feature.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

	// 즉시 처리 알림
	CHEER_FRIEND("깨우기"),
	CERTIFICATION_UPLOADED("친구의 인증게시물 업로드"),
	POST_REACTION("게시물 리액션"),
	POST_COMMENT("게시물 댓글"),

	// 스케줄 알림
	CERTIFICATION_REMINDER("인증 독촉"),
	RUNNING_PROGRESS_REMINDER("러닝 진행 독촉"),
	NEW_USER_RUNNING_REMINDER("신규 가입 러닝 독촉"),
	NEW_USER_FRIEND_REMINDER("신규 가입 친구추가 독촉");

	private final String description;
}
