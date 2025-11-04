package com.sixpack.dorundorun.feature.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateUserProfileTransactionService {

	@Transactional
	public void updateProfile(User user, String nickname, String profileImageUrl) {
		user.updateProfile(nickname, profileImageUrl);
	}
}
