package com.sixpack.dorundorun.feature.auth.exception;

import com.sixpack.dorundorun.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

	DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "이미 존재하는 이메일입니다. [email: %s]");

	private final HttpStatus status;
	private final String message;
}
