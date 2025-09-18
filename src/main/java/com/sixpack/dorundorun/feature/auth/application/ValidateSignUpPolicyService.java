package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.auth.dto.request.SignUpRequest;
import com.sixpack.dorundorun.feature.auth.exception.AuthErrorCode;
import com.sixpack.dorundorun.feature.user.application.ExistsUsersByEmailService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateSignUpPolicyService {

	private final ExistsUsersByEmailService existsUsersByEmailService;

	public void validate(SignUpRequest request) {
		validateEmailDuplication(request.email());
	}

	private void validateEmailDuplication(String email) {
		boolean isExisted = existsUsersByEmailService.exists(email);
		if (isExisted) {
			throw AuthErrorCode.DUPLICATE_EMAIL.format(email);
		}
	}
}
