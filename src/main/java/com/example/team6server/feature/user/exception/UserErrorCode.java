package com.example.team6server.feature.user.exception;

import com.example.team6server.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements ErrorCode {

	NOT_FOUND_USER_BY_ID(HttpStatus.NOT_FOUND, "해당 ID에 해당하는 유저를 찾을 수 없습니다. [ID: %s]");

	private final HttpStatus status;
	private final String message;
}
