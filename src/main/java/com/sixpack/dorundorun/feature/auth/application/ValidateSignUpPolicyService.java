package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.auth.dto.request.SignUpRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidateSignUpPolicyService {

	public void validate(SignUpRequest request) {
		// TODO: 실제 검증 로직 구현 필요
		// 임시: 검증 로직 비활성화
	}
}
