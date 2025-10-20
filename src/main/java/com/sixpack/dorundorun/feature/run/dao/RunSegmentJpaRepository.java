package com.sixpack.dorundorun.feature.run.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sixpack.dorundorun.feature.run.domain.RunSegment;

public interface RunSegmentJpaRepository extends JpaRepository<RunSegment, Long> {

	List<RunSegment> findByRunSessionIdOrderByCreatedAtAsc(Long runSessionId);

	@Query("SELECT rs FROM RunSegment rs " +
		"JOIN rs.runSession sess " +
		"WHERE sess.user.id = :userId " +
		"ORDER BY rs.createdAt DESC " +
		"LIMIT 1")
	Optional<RunSegment> findTopByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
