package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.auth.exception.AuthErrorCode;
import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ValidatePhoneNumberService {

	private final UserJpaRepository userRepository;

	public void validate(String phoneNumber) {
		if (userRepository.existsByPhoneNumber(phoneNumber)) {
			throw AuthErrorCode.PHONE_NUMBER_ALREADY_EXISTS.format(phoneNumber);
		}
	}
}