package com.sixpack.dorundorun.feature.auth.api;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.feature.auth.dto.request.RefreshTokenRequest;
import com.sixpack.dorundorun.feature.auth.dto.request.SignUpRequest;
import com.sixpack.dorundorun.feature.auth.dto.request.SmsSendRequest;
import com.sixpack.dorundorun.feature.auth.dto.request.SmsVerificationRequest;
import com.sixpack.dorundorun.feature.auth.dto.response.SignUpResponse;
import com.sixpack.dorundorun.feature.auth.dto.response.SmsVerificationResponse;
import com.sixpack.dorundorun.feature.auth.dto.response.TokenResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "[Auth 관련]")
public interface AuthApi {

	@Operation(summary = "SMS 인증 코드 발송",
		description = "회원가입 또는 로그인 시 전화번호로 6자리 인증 코드를 발송합니다. 유효시간은 3분입니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "인증 코드 발송 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 전화번호 형식"),
		@ApiResponse(responseCode = "429", description = "너무 많은 요청 (1분에 1회 제한)")
	})
	DorunResponse<Void> sendSms(@Valid @RequestBody SmsSendRequest request);

	@Operation(summary = "SMS 인증 코드 확인",
		description = "발송된 인증 코드를 확인합니다. 기존 회원이면 자동 로그인(토큰 발급), 신규 회원이면 회원가입 필요 응답을 반환합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "인증 성공 - 기존 회원 (자동 로그인, 토큰 포함)"),
		@ApiResponse(responseCode = "201", description = "인증 성공 - 신규 회원 (회원가입 필요)"),
		@ApiResponse(responseCode = "400", description = "인증 코드 불일치"),
		@ApiResponse(responseCode = "410", description = "인증 코드 만료 (3분 초과)")
	})
	DorunResponse<SmsVerificationResponse> verifySms(@Valid @RequestBody SmsVerificationRequest request);

	@Operation(summary = "회원가입",
		description = "SMS 인증을 완료한 신규 회원의 프로필 정보를 등록합니다. 회원가입 완료 시 자동으로 로그인 처리되어 토큰을 발급합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "회원가입 성공 (토큰 포함)"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청 (전화번호 인증 미완료, 닉네임 중복 등)"),
		@ApiResponse(responseCode = "409", description = "이미 가입된 전화번호")
	})
	DorunResponse<SignUpResponse> signUp(
		@Parameter(
			description = "회원가입 데이터",
			required = true,
			schema = @Schema(
				implementation = SignUpRequest.class,
				type = "string",
				format = "json"
			)
		)
		@RequestPart(value = "data") String dataJson,

		@Parameter(description = "프로필 이미지", required = false)
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage
	);

	@Operation(summary = "로그아웃",
		description = "현재 로그인된 사용자를 로그아웃합니다. Refresh Token을 무효화합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "로그아웃 성공"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	})
	DorunResponse<Void> logout(@Parameter(hidden = true) @CurrentUser User user);

	@Operation(summary = "토큰 갱신",
		description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
		@ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
	})
	DorunResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request);

	@Operation(summary = "회원 탈퇴",
		description = "현재 로그인된 사용자를 탈퇴 처리합니다. deletedAt에 현재 시각이 저장되며 데이터는 유지됩니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
		@ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
	})
	DorunResponse<Void> withdraw(@Parameter(hidden = true) @CurrentUser User user);
}
