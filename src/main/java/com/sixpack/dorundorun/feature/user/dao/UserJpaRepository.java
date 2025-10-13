package com.sixpack.dorundorun.feature.user.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sixpack.dorundorun.feature.user.domain.User;

public interface UserJpaRepository extends JpaRepository<User, Long> {
}
