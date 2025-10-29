package com.sixpack.dorundorun.feature.run.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sixpack.dorundorun.feature.run.dao.projection.RunSessionDetailProjection;
import com.sixpack.dorundorun.feature.run.dao.projection.RunSessionWithFeedProjection;
import com.sixpack.dorundorun.feature.run.domain.RunSession;

public interface RunSessionJpaRepository extends JpaRepository<RunSession, Long> {

	Optional<RunSession> findByUserIdAndFinishedAtIsNull(Long userId);

	@Query("""
		SELECT rs.id as id,
		       rs.createdAt as createdAt,
		       rs.updatedAt as updatedAt,
		       rs.finishedAt as finishedAt,
		       rs.distanceTotal as distanceTotal,
		       rs.durationTotal as durationTotal,
		       rs.paceAvg as paceAvg,
		       rs.cadenceAvg as cadenceAvg,
		       rs.mapImage as mapImage,
		       CASE WHEN f.id IS NOT NULL THEN true ELSE false END as isSelfied
		FROM RunSession rs
		LEFT JOIN Feed f ON rs.id = f.runSession.id AND f.deletedAt IS NULL
		WHERE rs.user.id = :userId
		 AND rs.finishedAt IS NOT NULL
		 AND (
			 :startDateTime IS NULL
			 OR rs.createdAt >= :startDateTime
		 )
		 AND (
			 :isSelfied IS NULL
			 OR(:isSelfied = true AND f.id IS NOT NULL)
		     OR (:isSelfied = false AND f.id IS NULL)
		 )
		ORDER BY rs.createdAt DESC
		""")
	List<RunSessionWithFeedProjection> findAllBySelfiedStatusAndStartDateTime(
		@Param("userId") Long userId,
		@Param("isSelfied") Boolean isSelfied,
		@Param("startDateTime") LocalDateTime startDateTime
	);

	Optional<RunSession> findByIdAndUserId(Long id, Long userId);

	@Query("""
		SELECT rs.id as id,
		       rs.createdAt as createdAt,
		       rs.updatedAt as updatedAt,
		       rs.finishedAt as finishedAt,
		       rs.distanceTotal as distanceTotal,
		       rs.durationTotal as durationTotal,
		       rs.paceAvg as paceAvg,
		       rs.paceMax as paceMax,
		       rs.paceMaxLatitude as paceMaxLatitude,
		       rs.paceMaxLongitude as paceMaxLongitude,
		       rs.cadenceAvg as cadenceAvg,
		       rs.cadenceMax as cadenceMax,
		       f.id as feedId,
		       f.mapImage as feedMapImage,
		       f.selfieImage as feedSelfieImage,
		       f.content as feedContent,
		       f.createdAt as feedCreatedAt
		FROM RunSession rs
		LEFT JOIN Feed f ON rs.id = f.runSession.id AND f.deletedAt IS NULL
		WHERE rs.id = :sessionId AND rs.user.id = :userId
		""")
	Optional<RunSessionDetailProjection> findDetailByIdAndUserId(
		@Param("sessionId") Long sessionId,
		@Param("userId") Long userId
	);

	@Query("""
		SELECT COALESCE(SUM(rs.distanceTotal), 0)
		FROM RunSession rs
		WHERE rs.user.id = :userId
		AND rs.finishedAt IS NOT NULL
		""")
	Long sumDistanceByUserId(@Param("userId") Long userId);

	@Query(value = """
		SELECT rs.id, rs.user_id
		FROM run_session rs
		LEFT JOIN feed f ON rs.id = f.run_session_id AND f.deleted_at IS NULL
		WHERE rs.finished_at IS NOT NULL
		  AND f.id IS NULL
		  AND rs.created_at <= DATE_SUB(NOW(), INTERVAL 23 HOUR)
		  AND rs.user_id IN (
		    SELECT u.id FROM users u WHERE u.deleted_at IS NULL
		  )
		""", nativeQuery = true)
	List<Object[]> findUnuploadedRunSessionsAfter23Hours();

	@Query(value = """
		SELECT DISTINCT u.id
		FROM users u
		LEFT JOIN run_session rs ON u.id = rs.user_id
		  AND rs.finished_at IS NOT NULL
		WHERE u.deleted_at IS NULL
		  AND (rs.id IS NULL OR rs.finished_at < DATE_SUB(NOW(), INTERVAL 7 DAY))
		  AND NOT EXISTS (
		    SELECT 1 FROM run_session rs2
		    WHERE rs2.user_id = u.id
		      AND rs2.finished_at IS NOT NULL
		      AND rs2.finished_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
		  )
		""", nativeQuery = true)
	List<Long> findUserIdsWithNoRecentRun();
}
