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
		description = """
			회원가입 또는 로그인 시 전화번호로 6자리 인증 코드를 발송합니다. 유효시간은 3분입니다.

			**개발/테스트용 번호:**
			- 전화번호: 000-1111-2222 (하이픈 제거: 00011112222)
			- 고정 인증번호: 000000
			- 실제 SMS 발송 없음
			- 발송 횟수 제한 없음

			**앱스토어 심사용 번호 (Android):**
			- 전화번호: 000-1111-1111 (하이픈 제거: 00011111111)
			- 고정 인증번호: 000000
			- 실제 SMS 발송 없음
			- 발송 횟수 제한 없음

			**앱스토어 심사용 번호 (iOS):**
			- 전화번호: 000-2222-2222 (하이픈 제거: 00022222222)
			- 고정 인증번호: 000000
			- 실제 SMS 발송 없음
			- 발송 횟수 제한 없음

			**일반 사용자:**
			- 실제 전화번호 입력
			- 랜덤 6자리 인증번호 생성
			- 실제 SMS 발송
			- 하루 최대 5회 발송 제한
			""")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK - 인증 코드 발송 성공"),
		@ApiResponse(responseCode = "400", description = "BAD_REQUEST - 올바르지 않은 전화번호 형식입니다"),
		@ApiResponse(responseCode = "429", description = "TOO_MANY_REQUESTS - 하루 5회까지만 인증번호를 발송할 수 있습니다"),
		@ApiResponse(responseCode = "502", description = "BAD_GATEWAY - SMS 발송에 실패했습니다")
	})
	DorunResponse<Void> sendSms(@Valid @RequestBody SmsSendRequest request);

	@Operation(summary = "SMS 인증 코드 확인",
		description = """
			발송된 인증 코드를 확인합니다. 기존 회원이면 자동 로그인(토큰 발급), 신규 회원이면 회원가입 필요 응답을 반환합니다.

			**개발/테스트용 번호:**
			- 전화번호: 000-1111-2222 (하이픈 제거: 00011112222)
			- 고정 인증번호: 000000

			**앱스토어 심사용 번호 (Android):**
			- 전화번호: 000-1111-1111 (하이픈 제거: 00011111111)
			- 고정 인증번호: 000000

			**앱스토어 심사용 번호 (iOS):**
			- 전화번호: 000-2222-2222 (하이픈 제거: 00022222222)
			- 고정 인증번호: 000000

			**일반 사용자:**
			- SMS로 발송받은 6자리 인증번호 입력
			- 3분 이내 인증 완료 필요
			- 최대 5회 시도 가능

			**토큰 처리 방법:**
			- accessToken: Authorization 헤더에 'Bearer {accessToken}' 형식으로 포함하여 API 요청
			- refreshToken: 액세스 토큰 만료 시 /api/auth/refresh 엔드포인트로 새 토큰 발급

			응답의 isExistingUser 필드로 기존/신규 회원 구분 가능합니다.
			""")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK - 인증 성공 (기존 회원: 토큰 포함, 신규 회원: 회원가입 필요)"),
		@ApiResponse(responseCode = "400", description = "BAD_REQUEST - 유효하지 않은 인증 코드입니다"),
		@ApiResponse(responseCode = "410", description = "GONE - 인증 코드가 만료되었습니다"),
		@ApiResponse(responseCode = "429", description = "TOO_MANY_REQUESTS - 인증 시도 횟수를 초과했습니다")
	})
	DorunResponse<SmsVerificationResponse> verifySms(@Valid @RequestBody SmsVerificationRequest request);

	@Operation(summary = "회원가입",
		description = """
			SMS 인증을 완료한 신규 회원의 프로필 정보를 등록합니다. 회원가입 완료 시 자동으로 로그인 처리되어 토큰을 발급합니다.
			
			**토큰 처리 방법:**
			- accessToken: Authorization 헤더에 'Bearer {accessToken}' 형식으로 포함하여 API 요청
			- refreshToken: 액세스 토큰 만료 시 /api/auth/refresh 엔드포인트로 새 토큰 발급
			""")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "CREATED - 회원가입 성공 (토큰 포함)"),
		@ApiResponse(responseCode = "400", description = "BAD_REQUEST - 올바르지 않은 전화번호 형식 또는 전화번호 인증 미완료"),
		@ApiResponse(responseCode = "409", description = "CONFLICT - 이미 가입된 전화번호입니다"),
		@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR - 사용자 코드 생성에 실패했습니다")
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
		@ApiResponse(responseCode = "200", description = "OK - 로그아웃 성공"),
		@ApiResponse(responseCode = "401", description = "UNAUTHORIZED - 유효하지 않거나 만료되었거나 누락된 토큰")
	})
	DorunResponse<Void> logout(@Parameter(hidden = true) @CurrentUser User user);

	@Operation(summary = "토큰 갱신",
		description = """
			Refresh Token으로 새로운 Access Token을 발급받습니다.
			
			**토큰 처리 방법:**
			- 발급받은 새로운 accessToken과 refreshToken으로 기존 토큰을 교체
			- accessToken: Authorization 헤더에 'Bearer {accessToken}' 형식으로 포함하여 API 요청
			- refreshToken: 다음 토큰 갱신 시 사용
			""")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK - 토큰 갱신 성공"),
		@ApiResponse(responseCode = "401", description = "UNAUTHORIZED - 유효하지 않거나 만료된 토큰, 또는 Refresh Token을 찾을 수 없음"),
		@ApiResponse(responseCode = "404", description = "NOT_FOUND - 사용자를 찾을 수 없습니다")
	})
	DorunResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request);

	@Operation(summary = "회원 탈퇴",
		description = "현재 로그인된 사용자를 탈퇴 처리합니다. 모든 관련 데이터(Feed, Friend, RunSession, Notification 등)와 S3 이미지가 완전히 삭제됩니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK - 회원 탈퇴 성공"),
		@ApiResponse(responseCode = "401", description = "UNAUTHORIZED - 유효하지 않거나 만료되었거나 누락된 토큰")
	})
	DorunResponse<Void> withdraw(@Parameter(hidden = true) @CurrentUser User user);
}
