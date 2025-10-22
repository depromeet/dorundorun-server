package com.sixpack.dorundorun.feature.feed.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sixpack.dorundorun.feature.feed.domain.Feed;

public interface FeedJpaRepository extends JpaRepository<Feed, Long> {

	Optional<Feed> findByRunSessionIdAndDeletedAtIsNull(Long runSessionId);

	// 특정 날짜의 특정 유저 피드 조회
	@Query("SELECT f FROM Feed f " +
		"JOIN FETCH f.user " +
		"JOIN FETCH f.runSession " +
		"WHERE f.user.id = :userId " +
		"AND f.createdAt BETWEEN :startDate AND :endDate " +
		"AND f.deletedAt IS NULL " +
		"ORDER BY f.createdAt DESC")
	List<Feed> findByUserIdAndDateRange(
		@Param("userId") Long userId,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	// 특정 날짜의 친구들 피드 조회 (TODO: Friend 테이블 연동 필요)
	@Query("SELECT f FROM Feed f " +
		"JOIN FETCH f.user " +
		"JOIN FETCH f.runSession " +
		"WHERE f.createdAt BETWEEN :startDate AND :endDate " +
		"AND f.deletedAt IS NULL " +
		"ORDER BY f.createdAt DESC")
	List<Feed> findByDateRange(
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	// 특정 유저의 인증 횟수 조회
	long countByUserIdAndDeletedAtIsNull(Long userId);
}
