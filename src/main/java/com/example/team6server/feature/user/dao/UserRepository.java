package com.example.team6server.feature.user.dao;

import com.example.team6server.feature.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByEmail(String email);
}
