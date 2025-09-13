package com.sixpack.dorundorun.feature.user.dao;

import com.sixpack.dorundorun.feature.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);
}
