package com.sixpack.dorundorun.feature.auth.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sixpack.dorundorun.feature.auth.application.SignUpService;
import com.sixpack.dorundorun.feature.auth.dto.request.SignUpRequest;
import com.sixpack.dorundorun.feature.auth.dto.response.SignUpResponse;
import com.sixpack.dorundorun.global.response.DorunResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

	private final SignUpService signUpService;

	@PostMapping("/api/auth/signup")
	public DorunResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
		SignUpResponse response = signUpService.signUp(request);
		return DorunResponse.success(response);
	}
}
