package com.sixpack.dorundorun.infra.firebase;

import org.springframework.http.HttpStatus;

import com.sixpack.dorundorun.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FcmErrorCode implements ErrorCode {

	FCM_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FCM 푸시 알림 전송에 실패했습니다."),
	FCM_INITIALIZATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Firebase 초기화에 실패했습니다."),

	FCM_DISABLED(HttpStatus.SERVICE_UNAVAILABLE, "FCM 서비스가 비활성화되어 있습니다."),

	INVALID_DEVICE_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 기기 토큰입니다."),
	INVALID_NOTIFICATION_DATA(HttpStatus.BAD_REQUEST, "유효하지 않은 알림 데이터입니다."),

	DEVICE_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자의 기기 토큰을 찾을 수 없습니다.");

	private final HttpStatus status;
	private final String message;
}
