package com.sixpack.dorundorun.feature.run.dao;

import com.sixpack.dorundorun.feature.run.domain.RunSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RunSessionJpaRepository extends JpaRepository<RunSession, Long> {
}
