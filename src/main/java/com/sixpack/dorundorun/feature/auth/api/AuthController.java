package com.sixpack.dorundorun.feature.auth.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.feature.auth.application.LogoutService;
import com.sixpack.dorundorun.feature.auth.application.RefreshAccessTokenService;
import com.sixpack.dorundorun.feature.auth.application.SignUpService;
import com.sixpack.dorundorun.feature.auth.application.SmsSendService;
import com.sixpack.dorundorun.feature.auth.application.SmsVerificationService;
import com.sixpack.dorundorun.feature.auth.application.WithdrawService;
import com.sixpack.dorundorun.feature.auth.dto.request.RefreshTokenRequest;
import com.sixpack.dorundorun.feature.auth.dto.request.SmsSendRequest;
import com.sixpack.dorundorun.feature.auth.dto.request.SmsVerificationRequest;
import com.sixpack.dorundorun.feature.auth.dto.response.SignUpResponse;
import com.sixpack.dorundorun.feature.auth.dto.response.SmsVerificationResponse;
import com.sixpack.dorundorun.feature.auth.dto.response.TokenResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

	private final RefreshAccessTokenService refreshAccessTokenService;
	private final SignUpService signUpService;
	private final SmsSendService smsSendService;
	private final SmsVerificationService smsVerificationService;
	private final LogoutService logoutService;
	private final WithdrawService withdrawService;

	@Override
	@PostMapping("/sms/send")
	public DorunResponse<Void> sendSms(@Valid @RequestBody SmsSendRequest request) {
		smsSendService.sendVerificationCode(request);
		return DorunResponse.success("인증 코드가 발송되었습니다");
	}

	@Override
	@PostMapping("/sms/verify")
	public DorunResponse<SmsVerificationResponse> verifySms(@Valid @RequestBody SmsVerificationRequest request) {
		SmsVerificationResponse response = smsVerificationService.verifyCode(request);
		if (response.isExistingUser()) {
			return DorunResponse.success("인증에 성공하였습니다", response);
		} else {
			return DorunResponse.success("인증에 성공하였습니다. 회원가입을 진행해주세요", response);
		}
	}

	@Override
	@PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DorunResponse<SignUpResponse> signUp(
		@RequestPart(value = "data") String dataJson,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage
	) {
		SignUpResponse response = signUpService.signUp(dataJson, profileImage);
		return DorunResponse.created("회원가입에 성공하였습니다", response);
	}

	@Override
	@PostMapping("/logout")
	public DorunResponse<Void> logout(@CurrentUser User user) {
		logoutService.logout(user);
		return DorunResponse.success("로그아웃에 성공하였습니다");
	}

	@Override
	@PostMapping("/refresh")
	public DorunResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		TokenResponse response = refreshAccessTokenService.refresh(request);
		return DorunResponse.success("토큰 갱신에 성공하였습니다", response);
	}

	@Override
	@DeleteMapping("/withdraw")
	public DorunResponse<Void> withdraw(@CurrentUser User user) {
		withdrawService.withdraw(user);
		return DorunResponse.success("회원 탈퇴가 완료되었습니다");
	}
}
