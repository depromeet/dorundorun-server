package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.security.crypto.password.PasswordEncoder;
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
	private final PasswordEncoder passwordEncoder;

	private final RedisStreamPublisher eventPublisher;

	@Transactional
	public SignUpResponse signUp(SignUpRequest request) {
		validateSignUpPolicyService.validate(request);

		User user = User.builder()
			.name(request.name())
			.email(request.email())
			.password(passwordEncoder.encode(request.password()))
			.build();

		User savedUser = userJpaRepository.save(user);

		publishUserRegisteredEvent(savedUser);

		return SignUpResponse.of(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
	}

	private void publishUserRegisteredEvent(User user) {
		UserRegisteredEvent event = UserRegisteredEvent.builder()
			.userId(user.getId())
			.email(user.getEmail())
			.name(user.getName())
			.build();

		eventPublisher.publishAfterCommit(event);
	}
}
