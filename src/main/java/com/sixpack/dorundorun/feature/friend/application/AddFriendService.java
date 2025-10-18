package com.sixpack.dorundorun.feature.friend.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.friend.dao.FriendJpaRepository;
import com.sixpack.dorundorun.feature.friend.domain.Friend;
import com.sixpack.dorundorun.feature.friend.exception.FriendErrorCode;
import com.sixpack.dorundorun.feature.user.application.FindUserByCodeService;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddFriendService {

	private final FindUserByIdService findUserByIdService;
	private final FindUserByCodeService findUserByCodeService;
	private final FindFriendByUserAndFriendService findFriendByUserAndFriendService;
	private final FriendJpaRepository friendJpaRepository;

	@Transactional
	public Friend add(Long userId, String friendCode) {
		User user = findUserByIdService.find(userId);
		User friendUser = findUserByCodeService.find(friendCode);

		validateCanAddFriend(user, friendUser);

		// 양방향 친구 관계 생성
		Friend userToFriend = createFriend(user, friendUser);
		Friend friendToUser = createFriend(friendUser, user);

		friendJpaRepository.save(userToFriend);
		friendJpaRepository.save(friendToUser);

		return userToFriend;
	}

	// 친구 추가 가능 여부 검증
	private void validateCanAddFriend(User user, User friendUser) {
		// 자기 자신을 친구로 추가할 수 없음
		if (user.getId().equals(friendUser.getId())) {
			throw FriendErrorCode.CANNOT_ADD_SELF_AS_FRIEND.format();
		}

		// 이미 친구인지 확인
		findFriendByUserAndFriendService.find(user, friendUser)
			.ifPresent(existingFriend -> {
				throw FriendErrorCode.ALREADY_FRIEND.format(friendUser.getId());
			});
	}

	// 새로운 친구 관계 생성
	private Friend createFriend(User user, User friendUser) {
		return Friend.builder()
			.user(user)
			.friend(friendUser)
			.build();
	}
}
