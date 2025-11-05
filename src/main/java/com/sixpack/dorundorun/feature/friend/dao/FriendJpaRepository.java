package com.sixpack.dorundorun.feature.friend.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sixpack.dorundorun.feature.friend.dao.projection.FriendRunningStatusProjection;
import com.sixpack.dorundorun.feature.friend.domain.Friend;
import com.sixpack.dorundorun.feature.user.domain.User;

public interface FriendJpaRepository extends JpaRepository<Friend, Long> {

	long countByUserIdAndDeletedAtIsNull(Long userId);

	@Query("SELECT f.friend.id FROM Friend f WHERE f.user.id = :userId AND f.deletedAt IS NULL")
	List<Long> findFriendIdsByUserId(@Param("userId") Long userId);

	Optional<Friend> findByUserAndFriendAndDeletedAtIsNull(User user, User friend);

	List<Friend> findByUserIdAndFriendIdInAndDeletedAtIsNull(Long userId, List<Long> friendIds);

	List<Friend> findByUserIdInAndFriendIdAndDeletedAtIsNull(List<Long> userIds, Long friendId);

	@Query(value = """
		SELECT u.id as userId,
		       CASE WHEN u.id = :userId THEN 1 ELSE 0 END as isMe,
		       u.nickname as nickname,
		       CAST(NULL AS string) as profileImage,
		       rs.createdAt as latestRanAt,
		       CAST(NULL AS long) as distance,
		       CAST(NULL AS double) as latitude,
		       CAST(NULL AS double) as longitude,
		       rs.data as runSegmentData
		FROM User u
		LEFT JOIN Friend f ON f.friend.id = u.id AND f.user.id = :userId AND f.deletedAt IS NULL
		LEFT JOIN RunSegment rs ON rs.id = (
		    SELECT rs2.id 
		    FROM RunSegment rs2 
		    JOIN rs2.runSession sess 
		    WHERE sess.user.id = u.id 
		    ORDER BY rs2.createdAt DESC 
		    LIMIT 1
		)
		WHERE u.id = :userId OR f.id IS NOT NULL
		ORDER BY CASE WHEN u.id = :userId THEN 0 ELSE 1 END, rs.createdAt DESC NULLS LAST
		""",
		countQuery = """
			SELECT COUNT(u) + 1
			FROM User u
			LEFT JOIN Friend f ON f.friend.id = u.id AND f.user.id = :userId AND f.deletedAt IS NULL
			WHERE u.id = :userId OR f.id IS NOT NULL
			""")
	Page<FriendRunningStatusProjection> findFriendsRunningStatus(@Param("userId") Long userId, Pageable pageable);

	int deleteByUserId(Long userId);

	int deleteByFriendId(Long friendId);

	@Query("""
		SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
		FROM Friend f
		WHERE f.user.id = :userId
		  AND f.deletedAt IS NULL
		""")
	boolean existsByUserIdAndDeletedAtIsNull(@Param("userId") Long userId);
}
