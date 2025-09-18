package com.sixpack.dorundorun.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {

	HttpStatus getStatus();

	String getMessage();

	default String formatted(Object... args) {
		return (args == null || args.length == 0)
			? getMessage()
			: String.format(getMessage(), args);
	}

	default CustomException format(Object... args) {
		return new CustomException(this, formatted(args));
	}
}
