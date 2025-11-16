package com.sixpack.dorundorun.infra.redis.sms;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.sixpack.dorundorun.feature.auth.exception.AuthErrorCode;
import com.sixpack.dorundorun.global.exception.CustomException;
import com.sixpack.dorundorun.global.utils.PhoneNumberMaskUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SMS 인증 코드 생성 및 검증을 담당하는 Redis 기반 관리자
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsVerificationCodeManager {

	private static final String CODE_KEY_PREFIX = "sms:verification:";
	private static final String DAILY_SEND_COUNT_KEY_PREFIX = "sms:daily:send:";
	private static final String ATTEMPT_KEY_PREFIX = "sms:attempt:";

	private static final int CODE_LENGTH = 6;
	private static final Duration CODE_EXPIRATION = Duration.ofMinutes(3);
	private static final int MAX_DAILY_SEND_COUNT = 5;
	private static final int MAX_VERIFICATION_ATTEMPTS = 10;
	private static final SecureRandom RANDOM = new SecureRandom();

	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * 6자리 랜덤 인증 코드 생성
	 */
	public String generateCode() {
		int code = RANDOM.nextInt(1000000); // 0 ~ 999999
		return String.format("%06d", code);
	}

	/**
	 * 자정까지 남은 시간 계산 (TTL용)
	 *
	 * @return 자정까지 남은 Duration
	 */
	private Duration getTimeUntilMidnight() {
		LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
		LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
		return Duration.between(now, midnight);
	}

	/**
	 * 하루 발송 횟수 제한 확인 (하루 5회)
	 *
	 * @param phoneNumber 전화번호
	 * @throws CustomException SMS_DAILY_LIMIT_EXCEEDED
	 */
	public void checkDailySendLimit(String phoneNumber) {
		String today = LocalDate.now(ZoneId.of("Asia/Seoul")).toString(); // YYYY-MM-DD
		String key = DAILY_SEND_COUNT_KEY_PREFIX + phoneNumber + ":" + today;

		String countStr = redisTemplate.opsForValue().get(key);
		int sendCount = (countStr != null) ? Integer.parseInt(countStr) : 0;

		if (sendCount >= MAX_DAILY_SEND_COUNT) {
			log.warn("Daily SMS send limit exceeded for phone: {}, count: {}/{}",
				phoneNumber, sendCount, MAX_DAILY_SEND_COUNT);
			throw new CustomException(AuthErrorCode.SMS_DAILY_LIMIT_EXCEEDED);
		}

		log.info("Daily SMS send count for phone: {}/{}", sendCount + 1, MAX_DAILY_SEND_COUNT);
	}

	/**
	 * 하루 발송 횟수 증가 (자정까지 TTL)
	 *
	 * @param phoneNumber 전화번호
	 */
	public void incrementDailySendCount(String phoneNumber) {
		String today = LocalDate.now(ZoneId.of("Asia/Seoul")).toString(); // YYYY-MM-DD
		String key = DAILY_SEND_COUNT_KEY_PREFIX + phoneNumber + ":" + today;

		String countStr = redisTemplate.opsForValue().get(key);
		int newCount = (countStr != null) ? Integer.parseInt(countStr) + 1 : 1;

		// 자정까지 TTL 설정
		Duration ttl = getTimeUntilMidnight();
		redisTemplate.opsForValue().set(key, String.valueOf(newCount), ttl);

		log.info("Daily SMS send count incremented to {}/{}, expires at midnight",
			newCount, MAX_DAILY_SEND_COUNT);
	}

	/**
	 * 인증 코드 저장 (3분 TTL)
	 *
	 * @param phoneNumber        전화번호
	 * @param verificationCode   인증 코드
	 */
	public void saveCode(String phoneNumber, String verificationCode) {
		String key = CODE_KEY_PREFIX + phoneNumber;
		redisTemplate.opsForValue().set(key, verificationCode, CODE_EXPIRATION);

		// 인증 시도 횟수 초기화
		String attemptKey = ATTEMPT_KEY_PREFIX + phoneNumber;
		redisTemplate.delete(attemptKey);

		log.info("Verification code saved, expires in {} minutes", CODE_EXPIRATION.toMinutes());
	}

	/**
	 * 인증 시도 횟수 확인 및 증가
	 *
	 * @param phoneNumber 전화번호
	 * @throws CustomException VERIFICATION_ATTEMPTS_EXCEEDED
	 */
	public void checkAndIncrementAttempts(String phoneNumber) {
		String key = ATTEMPT_KEY_PREFIX + phoneNumber;
		String attemptsStr = redisTemplate.opsForValue().get(key);

		int attempts = (attemptsStr != null) ? Integer.parseInt(attemptsStr) : 0;

		if (attempts >= MAX_VERIFICATION_ATTEMPTS) {
			log.warn("Verification attempts exceeded for phone: {}", PhoneNumberMaskUtil.mask(phoneNumber));
			throw new CustomException(AuthErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED);
		}

		// 시도 횟수 증가 (인증 코드와 동일한 TTL)
		redisTemplate.opsForValue().set(key, String.valueOf(attempts + 1), CODE_EXPIRATION);
		log.info("Verification attempt {}/{} for phone: {}", attempts + 1, MAX_VERIFICATION_ATTEMPTS, PhoneNumberMaskUtil.mask(phoneNumber));
	}

	/**
	 * 인증 코드 검증
	 *
	 * @param phoneNumber        전화번호
	 * @param verificationCode   사용자가 입력한 인증 코드
	 * @return 검증 성공 여부
	 */
	public boolean verifyCode(String phoneNumber, String verificationCode) {
		String key = CODE_KEY_PREFIX + phoneNumber;
		String savedCode = redisTemplate.opsForValue().get(key);

		if (savedCode == null) {
			log.warn("Verification code not found or expired for phone: {}", PhoneNumberMaskUtil.mask(phoneNumber));
			return false;
		}

		boolean isValid = savedCode.equals(verificationCode);
		if (isValid) {
			// 인증 성공 시 코드 및 시도 횟수 삭제 (재사용 방지)
			redisTemplate.delete(key);
			redisTemplate.delete(ATTEMPT_KEY_PREFIX + phoneNumber);
			log.info("Verification successful for phone: {}", PhoneNumberMaskUtil.mask(phoneNumber));
		} else {
			log.warn("Verification failed: incorrect code for phone: {}", PhoneNumberMaskUtil.mask(phoneNumber));
		}

		return isValid;
	}

	/**
	 * 인증 코드 삭제 (수동 취소 시)
	 *
	 * @param phoneNumber 전화번호
	 */
	public void deleteCode(String phoneNumber) {
		String codeKey = CODE_KEY_PREFIX + phoneNumber;
		String attemptKey = ATTEMPT_KEY_PREFIX + phoneNumber;
		redisTemplate.delete(codeKey);
		redisTemplate.delete(attemptKey);
		log.info("Verification code and attempts deleted for phone: {}", PhoneNumberMaskUtil.mask(phoneNumber));
	}
}
