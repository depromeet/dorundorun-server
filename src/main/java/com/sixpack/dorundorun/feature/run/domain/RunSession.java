package com.sixpack.dorundorun.feature.run.domain;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;
import com.sixpack.dorundorun.feature.goal.domain.GoalPlan;
import com.sixpack.dorundorun.feature.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "run_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RunSession extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "goal_plan_id")
	private GoalPlan goalPlan;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "total_distance")
	private Long totalDistance;

	@Column(name = "total_duration")
	private Long totalDuration;

	@Column(name = "avg_pace")
	private Long avgPace;

	@Column(name = "avg_cadence")
	private Integer avgCadence;

	@Column(name = "max_cadence")
	private Integer maxCadence;

	@Builder
	public RunSession(GoalPlan goalPlan, User user, Long totalDistance, Long totalDuration,
					  Long avgPace, Integer avgCadence, Integer maxCadence) {
		this.goalPlan = goalPlan;
		this.user = user;
		this.totalDistance = totalDistance;
		this.totalDuration = totalDuration;
		this.avgPace = avgPace;
		this.avgCadence = avgCadence;
		this.maxCadence = maxCadence;
	}
}