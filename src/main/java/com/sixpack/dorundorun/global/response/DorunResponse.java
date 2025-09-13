package com.sixpack.dorundorun.global.response;

import com.sixpack.dorundorun.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class DorunResponse<T> {

	private HttpStatus status;
	private String message;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime timestamp;

	private T data;

	// 성공 응답
	private DorunResponse(HttpStatus status, String message, T data) {
		this.status = status;
		this.message = message;
		this.timestamp = LocalDateTime.now();
		this.data = data;
	}

	// 에러 응답
	private DorunResponse(ErrorCode errorCode) {
		this.status = errorCode.getStatus();
		this.message = errorCode.getMessage();
		this.timestamp = LocalDateTime.now();
		this.data = null;
	}

	private DorunResponse(ErrorCode errorCode, String formattedMessage) {
		this.status = errorCode.getStatus();
		this.message = formattedMessage;
		this.timestamp = LocalDateTime.now();
		this.data = null;
	}

	// ===== 성공 응답 생성 메서드 =====
	public static <T> DorunResponse<T> success(T data) {
		return new DorunResponse<>(HttpStatus.OK, "요청이 성공적으로 처리되었습니다.", data);
	}

	public static <T> DorunResponse<T> success(String message, T data) {
		return new DorunResponse<>(HttpStatus.OK, message, data);
	}

	public static DorunResponse<Void> success(String message) {
		return new DorunResponse<>(HttpStatus.OK, message, null);
	}

	public static DorunResponse<Void> success() {
		return new DorunResponse<>(HttpStatus.OK, "요청이 성공적으로 처리되었습니다.", null);
	}

	public static <T> DorunResponse<T> created(String message, T data) {
		return new DorunResponse<>(HttpStatus.CREATED, message, data);
	}

	public static <T> DorunResponse<T> created(T data) {
		return new DorunResponse<>(HttpStatus.CREATED, "리소스가 성공적으로 생성되었습니다.", data);
	}

	// ===== 페이지네이션 응답 생성 메서드 =====
	public static <T> DorunResponse<PaginationResponse<T>> page(PaginationResponse<T> paginationResponse) {
		return new DorunResponse<>(HttpStatus.OK, "페이지 조회가 성공적으로 처리되었습니다.", paginationResponse);
	}

	public static <T> DorunResponse<PaginationResponse<T>> page(String message, PaginationResponse<T> paginationResponse) {
		return new DorunResponse<>(HttpStatus.OK, message, paginationResponse);
	}

	// ===== 에러 응답 생성 메서드 =====
	public static <T> DorunResponse<T> error(ErrorCode errorCode) {
		return new DorunResponse<>(errorCode);
	}

	public static <T> DorunResponse<T> error(ErrorCode errorCode, String formattedMessage) {
		return new DorunResponse<>(errorCode, formattedMessage);
	}
}
