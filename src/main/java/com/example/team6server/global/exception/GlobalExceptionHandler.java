package com.example.team6server.global.exception;

import com.example.team6server.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e, HttpServletRequest req) {
		log.warn("{} {} - {}", req.getMethod(), req.getRequestURI(), e.getMessage());
		return ResponseEntity
				.status(e.getErrorCode().getStatus())
				.body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleAllUnhandledException(Exception e, HttpServletRequest req) {
		log.error("Unhandled exception on {} {} ", req.getMethod(), req.getRequestURI(), e);
		return ResponseEntity
				.status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getStatus())
				.body(ApiResponse.error(GlobalErrorCode.INTERNAL_SERVER_ERROR));
	}
}
