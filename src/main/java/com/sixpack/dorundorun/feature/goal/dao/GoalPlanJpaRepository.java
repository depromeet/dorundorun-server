package com.sixpack.dorundorun.feature.goal.dao;

import com.sixpack.dorundorun.feature.goal.domain.GoalPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoalPlanJpaRepository extends JpaRepository<GoalPlan, Long> {
}
