package com.sixpack.dorundorun.feature.goal.domain;

import java.time.LocalDateTime;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "goal_plan")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class GoalPlan extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "goal_id", nullable = false)
	private Goal goal;

	@Column(name = "cleared_at")
	private LocalDateTime clearedAt;

	@Column(name = "pace", nullable = false)
	private Long pace;

	@Column(name = "distance", nullable = false)
	private Long distance;

	@Column(name = "duration", nullable = false)
	private Long duration;

	@Column(name = "round_count", nullable = false)
	private Integer roundCount;

	@Builder
	public GoalPlan(Goal goal,
		LocalDateTime clearedAt,
		Long pace,
		Long distance,
		Long duration,
		Integer roundCount) {
		this.goal = goal;
		this.clearedAt = clearedAt;
		this.pace = pace;
		this.distance = distance;
		this.duration = duration;
		this.roundCount = roundCount;
	}
}
