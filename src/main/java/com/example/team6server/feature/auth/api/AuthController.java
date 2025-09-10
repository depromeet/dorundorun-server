package com.example.team6server.feature.auth.api;

import com.example.team6server.feature.auth.application.SignUpService;
import com.example.team6server.feature.auth.dto.request.SignUpRequest;
import com.example.team6server.feature.auth.dto.response.SignUpResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

	private final SignUpService signUpService;

	@PostMapping("/api/auth/signup")
	public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
		SignUpResponse response = signUpService.signUp(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
