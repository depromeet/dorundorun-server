package com.example.team6server.feature.auth.application;

import com.example.team6server.feature.auth.dto.request.SignUpRequest;
import com.example.team6server.feature.auth.dto.response.SignUpResponse;
import com.example.team6server.feature.user.dao.UserRepository;
import com.example.team6server.feature.user.domain.User;
import com.example.team6server.feature.user.event.UserRegisteredEvent;
import com.example.team6server.infra.redis.stream.publisher.RedisStreamPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignUpService {

	private final ValidateSignUpPolicyService validateSignUpPolicyService;
	private final UserRepository userRepository;
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

		User savedUser = userRepository.save(user);

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
