package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.auth.dto.request.SignUpRequest;
import com.sixpack.dorundorun.feature.auth.dto.response.SignUpResponse;
import com.sixpack.dorundorun.feature.auth.exception.AuthErrorCode;
import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.user.event.UserRegisteredEvent;
import com.sixpack.dorundorun.global.config.jwt.JwtTokenProvider;
import com.sixpack.dorundorun.global.exception.CustomException;
import com.sixpack.dorundorun.global.utils.PhoneNumberNormalizationUtil;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;
import com.sixpack.dorundorun.infra.redis.token.RedisTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignUpService {

	private static final int MAX_CODE_GENERATION_ATTEMPTS = 3;

	private final ValidatePhoneNumberService validatePhoneNumberService;
	private final UploadProfileImageService uploadProfileImageService;
	private final GenerateUserCodeService generateUserCodeService;
	private final PhoneNumberNormalizationUtil phoneNumberNormalizationUtil;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTokenRepository redisTokenRepository;
	private final UserJpaRepository userJpaRepository;
	private final RedisStreamPublisher eventPublisher;
	private final ObjectMapper objectMapper;

	@Transactional
	public SignUpResponse signUp(String dataJson, MultipartFile profileImage) {
		SignUpRequest request = parseRequest(dataJson);

		String normalizedPhoneNumber = phoneNumberNormalizationUtil.normalize(request.phoneNumber());

		validatePhoneNumberService.validate(normalizedPhoneNumber);

		String profileImageUrl = uploadProfileImageService.upload(profileImage);
		String userCode = generateUniqueCode();

		User user = createUser(request, profileImageUrl, userCode, normalizedPhoneNumber);
		User savedUser = userJpaRepository.save(user);

		String accessToken = jwtTokenProvider.generateAccessToken(savedUser);
		String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser);

		redisTokenRepository.save(savedUser.getId(), refreshToken);

		publishUserRegisteredEvent(savedUser);

		log.info("User signed up successfully");

		return SignUpResponse.of(
			savedUser.getId(),
			savedUser.getNickname(),
			savedUser.getPhoneNumber(),
			accessToken,
			refreshToken
		);
	}

	private SignUpRequest parseRequest(String dataJson) {
		try {
			return objectMapper.readValue(dataJson, SignUpRequest.class);
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("Invalid JSON format", e);
		}
	}

	private User createUser(SignUpRequest request, String profileImageUrl, String userCode,
		String normalizedPhoneNumber) {
		return User.builder()
			.phoneNumber(normalizedPhoneNumber)
			.nickname(request.nickname())
			.profileImageUrl(profileImageUrl)
			.code(userCode)
			.deviceToken(request.deviceToken())
			.marketingConsentAt(request.consent() != null ? request.consent().marketingConsentAt() : null)
			.locationConsentAt(request.consent() != null ? request.consent().locationConsentAt() : null)
			.personalConsentAt(request.consent() != null ? request.consent().personalConsentAt() : null)
			.build();
	}

	private String generateUniqueCode() {
		for (int attempt = 0; attempt < MAX_CODE_GENERATION_ATTEMPTS; attempt++) {
			String code = generateUserCodeService.generate();

			if (!userJpaRepository.existsByCode(code)) {
				return code;
			}

			log.warn("User code collision detected: {}, retrying... (attempt {}/{})",
				code, attempt + 1, MAX_CODE_GENERATION_ATTEMPTS);
		}

		log.error("Failed to generate unique user code after {} attempts", MAX_CODE_GENERATION_ATTEMPTS);
		throw new CustomException(AuthErrorCode.USER_CODE_GENERATION_FAILED);
	}

	private void publishUserRegisteredEvent(User user) {
		UserRegisteredEvent event = UserRegisteredEvent.builder()
			.userId(user.getId())
			.phoneNumber(user.getPhoneNumber())
			.name(user.getNickname())
			.build();

		eventPublisher.publishAfterCommit(event);
	}
}
