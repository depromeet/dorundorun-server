package com.sixpack.dorundorun.feature.user.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sixpack.dorundorun.feature.user.domain.User;

public interface UserJpaRepository extends JpaRepository<User, Long> {

	Optional<User> findByCode(String code);
}
