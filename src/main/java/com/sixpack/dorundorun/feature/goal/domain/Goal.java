package com.sixpack.dorundorun.feature.goal.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;
import com.sixpack.dorundorun.feature.run.GoalRepeatType;
import com.sixpack.dorundorun.feature.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "goal")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Goal extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "started_at", nullable = false)
	private LocalDate startedAt;

	@Column(name = "ended_at", nullable = false)
	private LocalDate endedAt;

	@Column(name = "cleared_at")
	private LocalDateTime clearedAt;

	@Column(name = "pace")
	private Long pace;

	@Column(name = "distance")
	private Long distance;

	@Column(name = "duration")
	private Long duration;

	@Column(name = "repeat_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private GoalRepeatType repeatType;

	@Column(name = "repeat_frequency", nullable = false)
	private Integer repeatFrequency;

	@Builder
	public Goal(User user, String title, LocalDate startedAt, LocalDate endedAt,
		Long pace, Long distance, Long duration, GoalRepeatType repeatType, Integer repeatFrequency) {
		this.user = user;
		this.title = title;
		this.startedAt = startedAt;
		this.endedAt = endedAt;
		this.pace = pace;
		this.distance = distance;
		this.duration = duration;
		this.repeatType = repeatType;
		this.repeatFrequency = repeatFrequency;
	}
}
