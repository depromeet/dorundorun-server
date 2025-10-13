package com.sixpack.dorundorun.feature.auth.application;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.auth.dto.request.SignUpRequest;
import com.sixpack.dorundorun.feature.auth.dto.response.SignUpResponse;
import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.user.event.UserRegisteredEvent;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignUpService {

	private final ValidateSignUpPolicyService validateSignUpPolicyService;
	private final UserJpaRepository userJpaRepository;
	private final RedisStreamPublisher eventPublisher;

	@Transactional
	public SignUpResponse signUp(SignUpRequest request) {
		validateSignUpPolicyService.validate(request);

		// TODO: 실제 회원가입 로직 구현 필요
		User user = User.builder()
			.nickname(request.name()) // 임시: name을 nickname으로 사용
			.deviceToken("temp-device-token") // 임시: 기본 디바이스 토큰
			.personalConsentAt(LocalDateTime.now()) // 임시: 현재 시간으로 설정
			.build();

		User savedUser = userJpaRepository.save(user);

		publishUserRegisteredEvent(savedUser);

		return SignUpResponse.of(savedUser.getId(), savedUser.getNickname(), "temp-email@example.com");
	}

	private void publishUserRegisteredEvent(User user) {
		UserRegisteredEvent event = UserRegisteredEvent.builder()
			.userId(user.getId())
			.email("temp-email@example.com") // TODO: 추후 수정할 파트
			.name(user.getNickname()) // 임시
			.build();

		eventPublisher.publishAfterCommit(event);
	}
}
