package com.sixpack.dorundorun.feature.friend.dao;

import com.sixpack.dorundorun.feature.friend.dao.projection.FriendRunningStatusProjection;
import com.sixpack.dorundorun.feature.friend.domain.Friend;
import com.sixpack.dorundorun.feature.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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
                   u.profileImageUrl as profileImage,
                   rs.createdAt as latestRanAt,
                   rs.data as runSegmentData,
                   CAST(null as LocalDateTime) as latestCheeredAt
            FROM User u
            LEFT JOIN LATERAL (
                SELECT rs2.createdAt as createdAt, rs2.data as data
                FROM RunSegment rs2
                WHERE rs2.runSession.user.id = u.id
                  AND (rs2.runSession.manual = false OR rs2.runSession.manual IS NULL)
                ORDER BY rs2.createdAt DESC
                LIMIT 1
            ) rs
            WHERE u.id = :userId
               OR u.id IN (
                   SELECT f.friend.id FROM Friend f
                   WHERE f.user.id = :userId AND f.deletedAt IS NULL
               )
            ORDER BY CASE WHEN u.id = :userId THEN 0 ELSE 1 END, rs.createdAt DESC NULLS LAST
            """,
            countQuery = """
                    SELECT COUNT(u)
                    FROM User u
                    WHERE u.id = :userId
                       OR u.id IN (
                           SELECT f.friend.id FROM Friend f
                           WHERE f.user.id = :userId AND f.deletedAt IS NULL
                       )
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
