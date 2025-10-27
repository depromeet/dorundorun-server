package com.sixpack.dorundorun.feature.auth.exception;

import org.springframework.http.HttpStatus;

import com.sixpack.dorundorun.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements ErrorCode {

	// 토큰 관련
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다"),
	MISSING_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 필요합니다"),
	REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh Token을 찾을 수 없습니다"),

	// SMS 관련
	SMS_SEND_FAILED(HttpStatus.BAD_REQUEST, "SMS 발송에 실패했습니다"),
	SMS_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "1분에 1회만 요청 가능합니다"),
	VERIFICATION_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다"),
	VERIFICATION_CODE_EXPIRED(HttpStatus.GONE, "인증 코드가 만료되었습니다"),
	VERIFICATION_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "전화번호 인증을 먼저 완료해주세요"),

	// 회원가입 관련
	PHONE_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 전화번호입니다: %s"),
	NICKNAME_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "이미 사용 중인 닉네임입니다: %s"),
	INVALID_PHONE_NUMBER_FORMAT(HttpStatus.BAD_REQUEST, "올바르지 않은 전화번호 형식입니다"),
	USER_CODE_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "사용자 코드 생성에 실패했습니다"),

	// 회원 관련
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다"),
	USER_ALREADY_DELETED(HttpStatus.GONE, "이미 탈퇴한 사용자입니다");

	private final HttpStatus status;
	private final String message;
}
