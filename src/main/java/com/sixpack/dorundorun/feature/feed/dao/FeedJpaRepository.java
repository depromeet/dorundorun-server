package com.sixpack.dorundorun.feature.feed.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.dto.projection.FeedCountByDateProjection;

public interface FeedJpaRepository extends JpaRepository<Feed, Long> {

	Optional<Feed> findByRunSessionIdAndDeletedAtIsNull(Long runSessionId);

	@Query(value = """
		SELECT f FROM Feed f
		JOIN FETCH f.user
		JOIN FETCH f.runSession
		LEFT JOIN Friend fr ON fr.friend.id = f.user.id AND fr.user.id = :currentUserId AND fr.deletedAt IS NULL
		WHERE f.deletedAt IS NULL
		AND (:startDate IS NULL OR f.createdAt >= :startDate)
		AND (:endDate IS NULL OR f.createdAt <= :endDate)
		AND (
			(:userId IS NULL AND (fr.id IS NOT NULL OR f.user.id = :currentUserId))
			OR (:userId IS NOT NULL AND f.user.id = :userId)
		)
		ORDER BY f.createdAt DESC
		""",
		countQuery = """
			SELECT COUNT(f) FROM Feed f
			LEFT JOIN Friend fr ON fr.friend.id = f.user.id AND fr.user.id = :currentUserId AND fr.deletedAt IS NULL
			WHERE f.deletedAt IS NULL
			AND (:startDate IS NULL OR f.createdAt >= :startDate)
			AND (:endDate IS NULL OR f.createdAt <= :endDate)
			AND (
				(:userId IS NULL AND (fr.id IS NOT NULL OR f.user.id = :currentUserId))
				OR (:userId IS NOT NULL AND f.user.id = :userId)
			)
			""")
	Page<Feed> findByUserIdAndDateRangeWithReactions(
		@Param("userId") Long userId,
		@Param("currentUserId") Long currentUserId,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate,
		Pageable pageable
	);

	long countByUserIdAndDeletedAtIsNull(Long userId);

	@Query("""
		SELECT DATE(f.createdAt) as date, COUNT(f) as count
		FROM Feed f
		JOIN Friend fr ON fr.friend.id = f.user.id
		WHERE fr.user.id = :userId
		AND fr.deletedAt IS NULL
		AND (:startDate IS NULL OR f.createdAt >= :startDate)
		AND (:endDate IS NULL OR f.createdAt <= :endDate)
		AND f.deletedAt IS NULL
		GROUP BY DATE(f.createdAt)
		ORDER BY DATE(f.createdAt)
		""")
	List<FeedCountByDateProjection> countFriendFeedsByDateRange(
		@Param("userId") Long userId,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	@Query("""
		SELECT f FROM Feed f
		JOIN FETCH f.user
		LEFT JOIN Friend fr ON fr.friend.id = f.user.id AND fr.user.id = :currentUserId AND fr.deletedAt IS NULL
		WHERE f.deletedAt IS NULL
		AND (:startDate IS NULL OR f.createdAt >= :startDate)
		AND (:endDate IS NULL OR f.createdAt <= :endDate)
		AND (fr.id IS NOT NULL OR f.user.id = :currentUserId)
		ORDER BY f.createdAt DESC
		""")
	List<Feed> findByCurrentUserAndFriendsAndDateRange(
		@Param("currentUserId") Long currentUserId,
		@Param("startDate") LocalDateTime startDate,
		@Param("endDate") LocalDateTime endDate
	);

	List<Feed> findAllByUserId(Long userId);

	int deleteByUserId(Long userId);

	@Query("""
		SELECT f FROM Feed f
		JOIN FETCH f.user
		JOIN FETCH f.runSession
		WHERE f.id = :feedId
		AND f.deletedAt IS NULL
		""")
	Optional<Feed> findByIdWithUserAndRunSession(@Param("feedId") Long feedId);
}
