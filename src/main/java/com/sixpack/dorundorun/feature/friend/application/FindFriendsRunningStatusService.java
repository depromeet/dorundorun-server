package com.sixpack.dorundorun.feature.friend.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.friend.dao.FriendJpaRepository;
import com.sixpack.dorundorun.feature.friend.dao.projection.FriendRunningStatusProjection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindFriendsRunningStatusService {

	private final FriendJpaRepository friendJpaRepository;

	@Transactional(readOnly = true)
	public Page<FriendRunningStatusProjection> find(Long userId, Pageable pageable) {
		return friendJpaRepository.findFriendsRunningStatus(userId, pageable);
	}
}
