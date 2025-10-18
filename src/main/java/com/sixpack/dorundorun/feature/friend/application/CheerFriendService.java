package com.sixpack.dorundorun.feature.friend.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheerFriendService {

	private final FindUserByIdService findUserByIdService;

	@Transactional
	public void cheer(Long userId, Long friendUserId) {
		// 현재 유저 확인
		findUserByIdService.find(userId);

		// 응원할 친구 확인
		User friendUser = findUserByIdService.find(friendUserId);

		// TODO: 알림 전송 기능 구현 필요
		// sendCheerNotification(friendUser);
	}
}
