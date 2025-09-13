package com.sixpack.dorundorun.feature.run.dao;

import com.sixpack.dorundorun.feature.run.domain.RunSegment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunSegmentJpaRepository extends JpaRepository<RunSegment, Long> {
}
