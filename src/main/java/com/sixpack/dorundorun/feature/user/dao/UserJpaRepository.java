package com.sixpack.dorundorun.feature.user.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sixpack.dorundorun.feature.user.domain.User;

public interface UserJpaRepository extends JpaRepository<User, Long> {

	Optional<User> findByCode(String code);

	// 가입 후 24시간이 경과했지만 러닝을 시작하지 않은 사용자들 조회
	@Query(value = """
		SELECT u.id FROM users u
		WHERE u.deleted_at IS NULL
		  AND u.created_at < DATE_SUB(NOW(), INTERVAL 24 HOUR)
		  AND NOT EXISTS (
		    SELECT 1 FROM run_session rs
		    WHERE rs.user_id = u.id
		  )
		""", nativeQuery = true)
	List<Long> findUsersNotRunAfter24Hours();

	// 가입 후 48시간이 경과했지만 친구를 추가하지 않은 사용자들 조회
	@Query(value = """
		SELECT u.id FROM users u
		WHERE u.deleted_at IS NULL
		  AND u.created_at < DATE_SUB(NOW(), INTERVAL 48 HOUR)
		  AND NOT EXISTS (
		    SELECT 1 FROM friend f
		    WHERE f.user_id = u.id
		      AND f.deleted_at IS NULL
		  )
		""", nativeQuery = true)
	List<Long> findUsersNoFriendsAfter48Hours();

	boolean existsByCode(String code);

	boolean existsByPhoneNumber(String phoneNumber);

	Optional<User> findByPhoneNumber(String phoneNumber);
}
