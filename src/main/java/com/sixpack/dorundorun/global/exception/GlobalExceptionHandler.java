package com.sixpack.dorundorun.global.exception;

import com.sixpack.dorundorun.global.response.DorunResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<DorunResponse<Void>> handleCustomException(CustomException e, HttpServletRequest req) {
		log.warn("{} {} - {}", req.getMethod(), req.getRequestURI(), e.getMessage());
		return ResponseEntity
			.status(e.getErrorCode().getStatus())
			.body(DorunResponse.error(e.getErrorCode(), e.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<DorunResponse<Void>> handleAllUnhandledException(Exception e, HttpServletRequest req) {
		log.error("Unhandled exception on {} {} ", req.getMethod(), req.getRequestURI(), e);
		return ResponseEntity
			.status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
			.body(DorunResponse.error(GlobalErrorCode.INTERNAL_SERVER_ERROR));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<DorunResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
		HttpServletRequest req) {
		String message = e.getBindingResult().getFieldErrors().stream()
			.map(err -> err.getField() + ": " + err.getDefaultMessage())
			.collect(Collectors.joining(", "));
		log.warn("Validation failed on {} {} - {}", req.getMethod(), req.getRequestURI(), message);

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(DorunResponse.error(GlobalErrorCode.INVALID_INPUT, message));
	}
}
