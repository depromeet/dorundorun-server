package com.sixpack.dorundorun.feature.friend.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMyCodeService {

	private final FindUserByIdService findUserByIdService;

	@Transactional(readOnly = true)
	public String getCode(Long userId) {
		User user = findUserByIdService.find(userId);
		return user.getCode();
	}
}
