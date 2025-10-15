package com.sixpack.dorundorun.feature.run.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
		       CASE WHEN f.id IS NOT NULL THEN true ELSE false END as isSefied
		FROM RunSession rs
		LEFT JOIN Feed f ON rs.id = f.runSession.id AND f.deletedAt IS NULL
		WHERE rs.user.id = :userId
		 AND rs.finishedAt IS NOT NULL
		 AND (
			 :startDateTime IS NULL
			 OR rs.createdAt >= :startDateTime
		 )
		 AND (
			 :isSefied IS NULL
			 OR(:isSefied = true AND f.id IS NOT NULL)
		     OR (:isSefied = false AND f.id IS NULL)
		 )
		ORDER BY rs.createdAt DESC
		""")
	List<RunSessionWithFeedProjection> findAllBySefiedStatusAndStartDateTime(
		@Param("userId") Long userId,
		@Param("isSefied") Boolean isSefied,
		@Param("startDateTime") LocalDateTime startDateTime
	);
}
