package com.sixpack.dorundorun.feature.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.friend.exception.FriendErrorCode;
import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindUserByCodeService {

	private final UserJpaRepository userJpaRepository;

	@Transactional(readOnly = true)
	public User find(String code) {
		return userJpaRepository.findByCode(code)
			.orElseThrow(() -> FriendErrorCode.NOT_FOUND_USER_BY_CODE.format(code));
	}
}
