package com.sixpack.dorundorun.feature.user.exception;

import org.springframework.http.HttpStatus;

import com.sixpack.dorundorun.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

	NOT_FOUND_USER_BY_ID(HttpStatus.NOT_FOUND, "해당 ID에 해당하는 유저를 찾을 수 없습니다. [ID: %s]"),
	INVALID_PROFILE_UPDATE_REQUEST(HttpStatus.BAD_REQUEST, "올바르지 않은 프로필 수정 요청입니다."),
	PROFILE_IMAGE_REQUIRED(HttpStatus.BAD_REQUEST, "SET 옵션 사용 시 프로필 이미지 파일이 필요합니다.");

	private final HttpStatus status;
	private final String message;
}
