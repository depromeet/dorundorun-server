package com.sixpack.dorundorun.feature.goal.domain;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;
import com.sixpack.dorundorun.feature.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

	@Column(name = "title")
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

	@Column(name = "repeat_type")
	private String repeatType;

	@Column(name = "repeat_frequency")
	private Integer repeatFrequency;

	@Builder
	public Goal(User user, String title, LocalDate startedAt, LocalDate endedAt,
				Long pace, Long distance, Long duration, String repeatType, Integer repeatFrequency) {
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
