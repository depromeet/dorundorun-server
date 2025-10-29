package com.sixpack.dorundorun.feature.notification.exception;

import org.springframework.http.HttpStatus;

import com.sixpack.dorundorun.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements ErrorCode {

	NOT_FOUND_NOTIFICATION(HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다. [notificationId: %s]"),

	UNAUTHORIZED_NOTIFICATION_ACCESS(HttpStatus.FORBIDDEN, "이 알림에 접근할 권한이 없습니다. [notificationId: %s]"),

	INVALID_NOTIFICATION_STATUS(HttpStatus.BAD_REQUEST, "잘못된 알림 상태입니다. [status: %s]"),
	CANNOT_UPDATE_READ_NOTIFICATION(HttpStatus.BAD_REQUEST, "이미 읽은 알림은 수정할 수 없습니다. [notificationId: %s]"),

	FAILED_TO_SEND_NOTIFICATION(HttpStatus.INTERNAL_SERVER_ERROR, "알림 발송에 실패했습니다. [type: %s]");

	private final HttpStatus status;
	private final String message;
}
