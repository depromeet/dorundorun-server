package com.sixpack.dorundorun.feature.run.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sixpack.dorundorun.feature.run.domain.RunSession;

public interface RunSessionJpaRepository extends JpaRepository<RunSession, Long> {
}
