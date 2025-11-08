package com.sixpack.dorundorun.feature.run.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

	@Modifying
	@Query("DELETE FROM RunSegment rs WHERE rs.runSession.user.id = :userId")
	int deleteByUserId(@Param("userId") Long userId);

	@Modifying(clearAutomatically = true)
	@Query("DELETE FROM RunSegment rs WHERE rs.runSession.id = :runSessionId")
	void deleteByRunSessionId(@Param("runSessionId") Long runSessionId);
}
