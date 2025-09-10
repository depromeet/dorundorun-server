package com.example.team6server.feature.user.application;

import com.example.team6server.feature.user.dao.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExistsUsersByEmailService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public boolean exists(String email) {
		return userRepository.existsByEmail(email);
	}
}
