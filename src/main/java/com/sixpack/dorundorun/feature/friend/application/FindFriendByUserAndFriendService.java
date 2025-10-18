package com.sixpack.dorundorun.feature.friend.application;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.friend.dao.FriendJpaRepository;
import com.sixpack.dorundorun.feature.friend.domain.Friend;
import com.sixpack.dorundorun.feature.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFriendByUserAndFriendService {

	private final FriendJpaRepository friendJpaRepository;

	@Transactional(readOnly = true)
	public Optional<Friend> find(User user, User friend) {
		return friendJpaRepository.findByUserAndFriendAndDeletedAtIsNull(user, friend);
	}
}
