package com.sixpack.dorundorun.feature.friend.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.friend.dao.FriendJpaRepository;
import com.sixpack.dorundorun.feature.friend.domain.Friend;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFriendsByUserIdAndFriendIdsService {

	private final FriendJpaRepository friendJpaRepository;

	@Transactional(readOnly = true)
	public List<Friend> find(Long userId, List<Long> friendIds) {
		return friendJpaRepository.findByUserIdAndFriendIdInAndDeletedAtIsNull(userId, friendIds);
	}
}
