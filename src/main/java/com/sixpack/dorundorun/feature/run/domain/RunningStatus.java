package com.sixpack.dorundorun.feature.run.domain;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;
import com.sixpack.dorundorun.feature.user.domain.User;

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
@Table(name = "user_statistics")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RunningStatus extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "total_duration", nullable = false)
	private Long totalDuration;

	@Column(name = "total_distance", nullable = false)
	private Long totalDistance;

	@Column(name = "total_running", nullable = false)
	private Integer totalRunning;

	@Column(name = "total_goal_success", nullable = false)
	private Integer totalGoalSuccess;

	@Builder
	public RunningStatus(User user,
		Long totalDuration,
		Long totalDistance,
		Integer totalRunning,
		Integer totalGoalSuccess) {
		this.user = user;
		this.totalDuration = totalDuration;
		this.totalDistance = totalDistance;
		this.totalRunning = totalRunning;
		this.totalGoalSuccess = totalGoalSuccess;
	}
}