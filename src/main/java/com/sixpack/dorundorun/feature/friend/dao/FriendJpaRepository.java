package com.sixpack.dorundorun.feature.friend.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sixpack.dorundorun.feature.friend.domain.Friend;

public interface FriendJpaRepository extends JpaRepository<Friend, Long> {
}
