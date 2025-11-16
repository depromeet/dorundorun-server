package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.auth.dto.request.SmsVerificationRequest;
import com.sixpack.dorundorun.feature.auth.dto.response.SmsVerificationResponse;
import com.sixpack.dorundorun.feature.auth.exception.AuthErrorCode;
import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.config.jwt.JwtTokenProvider;
import com.sixpack.dorundorun.global.exception.CustomException;
import com.sixpack.dorundorun.global.utils.PhoneNumberNormalizationUtil;
import com.sixpack.dorundorun.infra.redis.sms.SmsVerificationCodeManager;
import com.sixpack.dorundorun.infra.redis.token.RedisTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SMS 인증 코드 검증 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsVerificationService {

	private final PhoneNumberNormalizationUtil phoneNumberNormalizationUtil;
	private final SmsVerificationCodeManager codeManager;
	private final UserJpaRepository userJpaRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTokenRepository redisTokenRepository;

	/**
	 * SMS 인증 코드 검증
	 * - 신규 회원: 회원가입 진행 가능 상태 반환
	 * - 기존 회원: 즉시 로그인 (토큰 발급)
	 *
	 * @param request 인증 확인 요청 (전화번호 + 인증 코드)
	 * @return 인증 결과 (기존 회원 여부, 토큰 정보 등)
	 */
	@Transactional
	public SmsVerificationResponse verifyCode(SmsVerificationRequest request) {
		String phoneNumber = phoneNumberNormalizationUtil.normalize(request.phoneNumber());
		String verificationCode = request.verificationCode();

		// 1. 임시 인증번호가 아닌 경우에만 인증 시도 횟수 확인 및 증가 (TODO: 런칭데이 이후 제거)
		if (!"000000".equals(verificationCode)) {
			codeManager.checkAndIncrementAttempts(phoneNumber);
		}

		// 2. 인증 코드 검증
		boolean isValid = codeManager.verifyCode(phoneNumber, verificationCode);
		if (!isValid) {
			throw new CustomException(AuthErrorCode.INVALID_VERIFICATION_CODE);
		}

		// 3. 기존 회원 여부 확인
		return userJpaRepository.findByPhoneNumber(phoneNumber)
			.map(user -> handleExistingUser(user, phoneNumber))
			.orElseGet(() -> handleNewUser(phoneNumber));
	}

	/**
	 * 기존 회원 처리: 즉시 로그인 (토큰 발급)
	 */
	private SmsVerificationResponse handleExistingUser(User user, String phoneNumber) {
		// Access Token 발급
		String accessToken = jwtTokenProvider.generateAccessToken(user);

		// Refresh Token 발급
		String refreshToken = jwtTokenProvider.generateRefreshToken(user);

		// Refresh Token Redis에 저장
		redisTokenRepository.save(user.getId(), refreshToken);

		log.info("Existing user logged in via SMS verification: userId={}", user.getId());

		return SmsVerificationResponse.ofExistingUser(
			phoneNumber,
			user.getId(),
			user.getNickname(),
			accessToken,
			refreshToken
		);
	}

	/**
	 * 신규 회원 처리: 회원가입 진행 필요
	 */
	private SmsVerificationResponse handleNewUser(String phoneNumber) {
		log.info("New user detected for signup");
		return SmsVerificationResponse.ofNewUser(phoneNumber);
	}
}
