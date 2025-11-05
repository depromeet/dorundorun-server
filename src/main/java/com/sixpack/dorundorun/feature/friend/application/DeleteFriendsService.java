package com.sixpack.dorundorun.feature.friend.application;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.friend.dao.FriendJpaRepository;
import com.sixpack.dorundorun.feature.friend.domain.Friend;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteFriendsService {

	private final FindUserByIdService findUserByIdService;
	private final FindFriendsByUserIdAndFriendIdsService findFriendsByUserIdAndFriendIdsService;
	private final FriendJpaRepository friendJpaRepository;

	@Transactional
	public Map<Long, String> delete(Long userId, List<Long> friendIds) {
		// 유저 존재 확인
		findUserByIdService.find(userId);

		// 삭제할 친구 목록 조회 (user -> friend)
		List<Friend> userToFriends = findFriendsByUserIdAndFriendIdsService.find(userId, friendIds);

		// 역방향 친구 목록 조회 (friend -> user)
		List<Friend> friendToUsers = friendJpaRepository.findByUserIdInAndFriendIdAndDeletedAtIsNull(friendIds, userId);

		LocalDateTime now = LocalDateTime.now();

		// 양방향 Soft delete 처리
		Map<Long, String> deletedFriends = new HashMap<>();
		userToFriends.forEach(friend -> {
			friend.delete(now);
			deletedFriends.put(friend.getFriend().getId(), friend.getFriend().getNickname());
		});
		friendToUsers.forEach(friend -> friend.delete(now));

		return deletedFriends;
	}
}
