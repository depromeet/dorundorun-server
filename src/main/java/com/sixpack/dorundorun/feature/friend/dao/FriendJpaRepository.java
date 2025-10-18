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

	Optional<Friend> findByUserAndFriendAndDeletedAtIsNull(User user, User friend);

	List<Friend> findByUserIdAndFriendIdInAndDeletedAtIsNull(Long userId, List<Long> friendIds);

	List<Friend> findByUserIdInAndFriendIdAndDeletedAtIsNull(List<Long> userIds, Long friendId);

	// 친구들의 러닝 현황 조회 (가장 최신 RunSegment 기준)
	// CTE를 사용하여 중복 계산 제거 및 가독성 향상
	@Query(value = """
		WITH latest_segment AS (
		    SELECT
		        u.id as user_id,
		        u.nickname,
		        rs.created_at as latest_ran_at,
		        rs.data,
		        CASE
		            WHEN rs.data IS NOT NULL AND JSON_LENGTH(rs.data, '$.segments') > 0
		            THEN JSON_LENGTH(rs.data, '$.segments') - 1
		            ELSE NULL
		        END as last_index
		    FROM friend f
		    JOIN users u ON f.friend_id = u.id
		    LEFT JOIN run_segment rs ON rs.id = (
		        SELECT rs2.id
		        FROM run_segment rs2
		        JOIN run_session rsess ON rs2.run_session_id = rsess.id
		        WHERE rsess.user_id = u.id
		        ORDER BY rs2.created_at DESC
		        LIMIT 1
		    )
		    WHERE f.user_id = :userId
		      AND f.deleted_at IS NULL
		)
		SELECT
		    user_id as userId,
		    CAST(0 AS UNSIGNED) as isMe,
		    nickname as nickname,
		    null as profileImage,
		    latest_ran_at as latestRanAt,
		    CASE
		        WHEN last_index IS NOT NULL
		        THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(data, CONCAT('$.segments[', last_index, '].distance'))) AS UNSIGNED)
		        ELSE NULL
		    END as distance,
		    CASE
		        WHEN last_index IS NOT NULL
		        THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(data, CONCAT('$.segments[', last_index, '].latitude'))) AS DECIMAL(10, 8))
		        ELSE NULL
		    END as latitude,
		    CASE
		        WHEN last_index IS NOT NULL
		        THEN CAST(JSON_UNQUOTE(JSON_EXTRACT(data, CONCAT('$.segments[', last_index, '].longitude'))) AS DECIMAL(11, 8))
		        ELSE NULL
		    END as longitude
		FROM latest_segment
		ORDER BY latest_ran_at IS NULL, latest_ran_at DESC
		""",
		countQuery = """
			SELECT COUNT(*)
			FROM friend f
			WHERE f.user_id = :userId
			  AND f.deleted_at IS NULL
			""",
		nativeQuery = true)
	Page<FriendRunningStatusProjection> findFriendsRunningStatus(@Param("userId") Long userId, Pageable pageable);
}
