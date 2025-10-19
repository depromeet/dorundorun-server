package com.sixpack.dorundorun.feature.auth.api;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

	// private final SignUpService signUpService;
	// TODO: 서비스 의존성 주입 예정
	// private final SmsSendService smsSendService;
	// private final SmsVerificationService smsVerificationService;
	// private final LogoutService logoutService;
	// private final TokenRefreshService tokenRefreshService;
	// private final WithdrawService withdrawService;

	@Override
	@PostMapping("/sms/send")
	public DorunResponse<Void> sendSms(@Valid @RequestBody SmsSendRequest request) {
		// TODO: 서비스 로직 구현 예정
		// smsSendService.sendVerificationCode(request);
		// return DorunResponse.success("인증 코드가 발송되었습니다");
		return DorunResponse.success("인증 코드가 발송되었습니다");
	}

	@Override
	@PostMapping("/sms/verify")
	public DorunResponse<SmsVerificationResponse> verifySms(@Valid @RequestBody SmsVerificationRequest request) {
		// TODO: 서비스 로직 구현 예정
		// SmsVerificationResponse response = smsVerificationService.verifyCode(request);
		// if (response.isExistingUser()) {
		//     return DorunResponse.success("인증에 성공하였습니다", response);
		// } else {
		//     return DorunResponse.created("인증에 성공하였습니다. 회원가입을 진행해주세요", response);
		// }
		return DorunResponse.success("인증에 성공하였습니다", null);
	}

	@Override
	@PostMapping("/signup")
	public DorunResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
		// SignUpResponse response = signUpService.signUp(request);
		// return DorunResponse.created(response);
		return DorunResponse.created(null);
	}

	@Override
	@PostMapping("/logout")
	public DorunResponse<Void> logout(@CurrentUser User user) {
		// TODO: 서비스 로직 구현 예정
		// logoutService.logout(user);
		// return DorunResponse.success("로그아웃에 성공하였습니다");
		return DorunResponse.success("로그아웃에 성공하였습니다");
	}

	@Override
	@PostMapping("/refresh")
	public DorunResponse<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		// TODO: 서비스 로직 구현 예정
		// TokenResponse response = tokenRefreshService.refresh(request);
		// return DorunResponse.success("토큰 갱신에 성공하였습니다", response);
		return DorunResponse.success("토큰 갱신에 성공하였습니다", null);
	}

	@Override
	@DeleteMapping("/withdraw")
	public DorunResponse<Void> withdraw(@CurrentUser User user) {
		// TODO: 서비스 로직 구현 예정
		// withdrawService.withdraw(user);
		// return DorunResponse.success("회원 탈퇴가 완료되었습니다");
		return DorunResponse.success("회원 탈퇴가 완료되었습니다");
	}
}
