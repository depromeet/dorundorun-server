package com.sixpack.dorundorun.infra.s3;

import org.springframework.http.HttpStatus;

import com.sixpack.dorundorun.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum S3ErrorCode implements ErrorCode {

	S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 업로드에 실패했습니다."),
	S3_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 다운로드에 실패했습니다."),
	S3_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 파일 삭제에 실패했습니다."),
	S3_URL_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3 URL 생성에 실패했습니다."),
	
	INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
	FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 제한을 초과했습니다."),
	FILE_IS_EMPTY(HttpStatus.BAD_REQUEST, "파일이 비어있습니다.");

	private final HttpStatus status;
	private final String message;
}