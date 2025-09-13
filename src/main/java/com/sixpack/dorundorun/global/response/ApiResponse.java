package com.sixpack.dorundorun.global.response;

import com.sixpack.dorundorun.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ApiResponse<T> {

	private HttpStatus status;
	private String message;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timestamp;

	private T data;

	// 성공 응답
	private ApiResponse(HttpStatus status, String message, T data) {
		this.status = status;
		this.message = message;
		this.timestamp = LocalDateTime.now();
		this.data = data;
	}

	// 에러 응답
	private ApiResponse(ErrorCode errorCode) {
		this.status = errorCode.getStatus();
		this.message = errorCode.getMessage();
		this.timestamp = LocalDateTime.now();
		this.data = null;
	}

	private ApiResponse(ErrorCode errorCode, String formattedMessage) {
		this.status = errorCode.getStatus();
		this.message = formattedMessage;
		this.timestamp = LocalDateTime.now();
		this.data = null;
	}

	// ===== 성공 응답 생성 메서드 =====
	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(HttpStatus.OK, "요청이 성공적으로 처리되었습니다.", data);
	}

	public static <T> ApiResponse<T> success(String message, T data) {
		return new ApiResponse<>(HttpStatus.OK, message, data);
	}

	public static ApiResponse<Void> success(String message) {
		return new ApiResponse<>(HttpStatus.OK, message, null);
	}

	public static ApiResponse<Void> success() {
		return new ApiResponse<>(HttpStatus.OK, "요청이 성공적으로 처리되었습니다.", null);
	}

	public static <T> ApiResponse<T> created(String message, T data) {
		return new ApiResponse<>(HttpStatus.CREATED, message, data);
	}

	public static <T> ApiResponse<T> created(T data) {
		return new ApiResponse<>(HttpStatus.CREATED, "리소스가 성공적으로 생성되었습니다.", data);
	}

	// ===== 에러 응답 생성 메서드 =====
	public static <T> ApiResponse<T> error(ErrorCode errorCode) {
		return new ApiResponse<>(errorCode);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode, String formattedMessage) {
		return new ApiResponse<>(errorCode, formattedMessage);
	}
}
