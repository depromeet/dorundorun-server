package com.sixpack.dorundorun.global.utils;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.auth.exception.AuthErrorCode;
import com.sixpack.dorundorun.global.exception.CustomException;

@Service
public class PhoneNumberNormalizationUtil {

	public String normalize(String phoneNumber) {
		if (phoneNumber == null || phoneNumber.isBlank()) {
			throw new CustomException(AuthErrorCode.INVALID_PHONE_NUMBER_FORMAT);
		}

		String normalized = phoneNumber.replaceAll("-", "");

		if (!normalized.matches("^\\d{11}$")) {
			throw new CustomException(AuthErrorCode.INVALID_PHONE_NUMBER_FORMAT);
		}

		return normalized;
	}

}