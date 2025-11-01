package com.sixpack.dorundorun.feature.feed.exception;

import org.springframework.http.HttpStatus;

import com.sixpack.dorundorun.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FeedErrorCode implements ErrorCode {

	ALREADY_EXISTS_FEED_FOR_RUN_SESSION(HttpStatus.BAD_REQUEST, "해당 러닝 세션에 대한 셀피가 이미 존재합니다. [runSessionId: %s]"),
	NOT_FOUND_FEED(HttpStatus.NOT_FOUND, "해당 피드를 찾을 수 없습니다. [feedId: %s]"),
	FORBIDDEN_FEED_ACCESS(HttpStatus.FORBIDDEN, "해당 피드에 대한 권한이 없습니다.");

	private final HttpStatus status;
	private final String message;
}
