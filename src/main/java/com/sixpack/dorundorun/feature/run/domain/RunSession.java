package com.sixpack.dorundorun.feature.run.domain;

import java.time.LocalDateTime;

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
@Table(name = "run_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RunSession extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "finished_at")
	private LocalDateTime finishedAt;

	@Column(name = "distance_total")
	private Long distanceTotal;

	@Column(name = "duration_total")
	private Long durationTotal;

	@Column(name = "pace_avg")
	private Double paceAvg;

	@Column(name = "pace_max")
	private Long paceMax;

	@Column(name = "pace_max_latitude")
	private Double paceMaxLatitude;

	@Column(name = "pace_max_longitude")
	private Double paceMaxLongitude;

	@Column(name = "cadence_avg")
	private Integer cadenceAvg;

	@Column(name = "cadence_max")
	private Integer cadenceMax;

	@Column(name = "is_retry", nullable = false)
	private boolean retry;

	@Builder
	public RunSession(User user,
		LocalDateTime finishedAt,
		Long distanceTotal,
		Long durationTotal,
		Double paceAvg,
		Long paceMax,
		Double paceMaxLatitude,
		Double paceMaxLongitude,
		Integer cadenceAvg,
		Integer cadenceMax,
		boolean retry) {
		this.user = user;
		this.finishedAt = finishedAt;
		this.distanceTotal = distanceTotal;
		this.durationTotal = durationTotal;
		this.paceAvg = paceAvg;
		this.paceMax = paceMax;
		this.paceMaxLatitude = paceMaxLatitude;
		this.paceMaxLongitude = paceMaxLongitude;
		this.cadenceAvg = cadenceAvg;
		this.cadenceMax = cadenceMax;
		this.retry = retry;
	}
}
