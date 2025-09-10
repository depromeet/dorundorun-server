package com.example.team6server.feature.auth.application;

import com.example.team6server.feature.auth.dto.request.SignUpRequest;
import com.example.team6server.feature.auth.dto.response.SignUpResponse;
import com.example.team6server.feature.user.dao.UserRepository;
import com.example.team6server.feature.user.domain.User;
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


	@Transactional
	public SignUpResponse signUp(SignUpRequest request) {
		validateSignUpPolicyService.validate(request);

		User user = User.builder()
				.name(request.name())
				.email(request.email())
				.password(passwordEncoder.encode(request.password()))
				.build();

		User savedUser = userRepository.save(user);

		return SignUpResponse.of(savedUser.getId(), savedUser.getName(), savedUser.getEmail());
	}
}
