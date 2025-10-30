package com.sixpack.dorundorun.feature.friend.exception;

import org.springframework.http.HttpStatus;

import com.sixpack.dorundorun.global.exception.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReverseGeocodingErrorCode implements ErrorCode {

	REVERSE_GEOCODING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "주소 변환에 실패했습니다. [latitude: %s, longitude: %s]"),
	CACHE_OPERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "주소 캐시 처리에 실패했습니다. [cacheKey: %s]");

	private final HttpStatus status;
	private final String message;
}
