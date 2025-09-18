package com.sixpack.dorundorun.feature.goal.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sixpack.dorundorun.feature.goal.domain.GoalPlan;

public interface GoalPlanJpaRepository extends JpaRepository<GoalPlan, Long> {
}
