package com.example.team6server.feature.user.application;

import com.example.team6server.feature.user.dao.UserRepository;
import com.example.team6server.feature.user.domain.User;
import com.example.team6server.feature.user.exception.UserErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class FindUserByIdService {

	private final UserRepository userRepository;

	@Transactional(readOnly = true)
	public User find(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> UserErrorCode.NOT_FOUND_USER_BY_ID.format(id));
	}
}
