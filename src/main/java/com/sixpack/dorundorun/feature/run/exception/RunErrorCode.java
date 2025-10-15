package com.sixpack.dorundorun.feature.run.exception;

import org.springframework.http.HttpStatus;

import com.sixpack.dorundorun.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RunErrorCode implements ErrorCode {

	ALREADY_EXISTS_ACTIVE_RUN_SESSION(HttpStatus.BAD_REQUEST, "이미 진행중인 러닝이 존재합니다. [runSessionId: %s]");

	private final HttpStatus status;
	private final String message;
}
