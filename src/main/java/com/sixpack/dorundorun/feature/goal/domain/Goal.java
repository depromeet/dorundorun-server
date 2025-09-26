package com.sixpack.dorundorun.feature.goal.domain;

import java.time.LocalDateTime;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;
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

	@Column(name = "sub_title", nullable = false)
	private String subTitle;

	@Column(name = "paused_at")
	private LocalDateTime pausedAt;

	@Column(name = "cleared_at")
	private LocalDateTime clearedAt;

	@Column(name = "pace")
	private Long pace;

	@Column(name = "distance")
	private Long distance;

	@Column(name = "duration")
	private Long duration;

	@Column(name = "total_round_count", nullable = false)
	private Integer totalRoundCount;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 32)
	private GoalType type;

	@Builder
	public Goal(User user,
		String title,
		String subTitle,
		LocalDateTime pausedAt,
		LocalDateTime clearedAt,
		Long pace,
		Long distance,
		Long duration,
		Integer totalRoundCount,
		GoalType type) {
		this.user = user;
		this.title = title;
		this.subTitle = subTitle;
		this.pausedAt = pausedAt;
		this.clearedAt = clearedAt;
		this.pace = pace;
		this.distance = distance;
		this.duration = duration;
		this.totalRoundCount = totalRoundCount;
		this.type = type;
	}
}
