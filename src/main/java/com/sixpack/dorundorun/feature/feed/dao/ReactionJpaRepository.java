package com.sixpack.dorundorun.feature.feed.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sixpack.dorundorun.feature.feed.domain.EmojiType;
import com.sixpack.dorundorun.feature.feed.domain.Reaction;

public interface ReactionJpaRepository extends JpaRepository<Reaction, Long> {

	// 특정 피드 목록의 모든 반응 조회
	@Query("SELECT r FROM Reaction r " +
		"JOIN FETCH r.user " +
		"WHERE r.feed.id IN :feedIds " +
		"AND r.deletedAt IS NULL " +
		"ORDER BY r.createdAt ASC")
	List<Reaction> findByFeedIdIn(@Param("feedIds") List<Long> feedIds);

	// 특정 사용자의 특정 피드에 대한 특정 이모지 반응 조회 (삭제된 것 포함)
	@Query("SELECT r FROM Reaction r " +
		"WHERE r.feed.id = :feedId " +
		"AND r.user.id = :userId " +
		"AND r.emojiType = :emojiType")
	Optional<Reaction> findByFeedIdAndUserIdAndEmojiType(
		@Param("feedId") Long feedId,
		@Param("userId") Long userId,
		@Param("emojiType") EmojiType emojiType
	);

	// 특정 피드의 활성화된 반응 개수 조회
	@Query("SELECT COUNT(r) FROM Reaction r " +
		"WHERE r.feed.id = :feedId " +
		"AND r.deletedAt IS NULL")
	int countByFeedIdAndDeletedAtIsNull(@Param("feedId") Long feedId);

	// 특정 사용자의 모든 Reaction 삭제 (회원 탈퇴용)
	int deleteByUserId(Long userId);
}
