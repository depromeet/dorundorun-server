package com.sixpack.dorundorun.feature.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExistsUsersByEmailService {

	private final UserJpaRepository userJpaRepository;

	@Transactional(readOnly = true)
	public boolean exists(String email) {
		return userJpaRepository.existsByEmail(email);
	}
}
