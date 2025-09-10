package com.example.team6server.feature.auth.api;

import com.example.team6server.feature.auth.dto.request.SignUpRequest;
import com.example.team6server.feature.auth.dto.response.SignUpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "[1. 인증 관련]")
public interface AuthApi {

	@Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "회원가입 성공"),
			@ApiResponse(responseCode = "400", description = "잘못된 요청[이메일 중복 등]"),
	})
	ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request);
}