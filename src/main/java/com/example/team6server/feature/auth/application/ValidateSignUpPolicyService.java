package com.example.team6server.feature.auth.application;

import com.example.team6server.feature.auth.dto.request.SignUpRequest;
import com.example.team6server.feature.auth.exception.AuthErrorCode;
import com.example.team6server.feature.user.application.ExistsUsersByEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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