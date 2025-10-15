package com.sixpack.dorundorun.feature.run.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sixpack.dorundorun.feature.run.domain.RunSegment;

public interface RunSegmentJpaRepository extends JpaRepository<RunSegment, Long> {

	List<RunSegment> findByRunSessionIdOrderByCreatedAtAsc(Long runSessionId);
}
