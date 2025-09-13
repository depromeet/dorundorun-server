package com.sixpack.dorundorun.feature.user.application;

import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExistsUsersByEmailService {

	private final UserJpaRepository userJpaRepository;

	@Transactional(readOnly = true)
	public boolean exists(String email) {
		return userJpaRepository.existsByEmail(email);
	}
}
