package com.sixpack.dorundorun.feature.run.domain;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "run_segment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RunSegment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "run_session_id", nullable = false)
	private RunSession runSession;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "data", nullable = false, columnDefinition = "json")
	private RunSegmentInfo data;

	@Builder
	public RunSegment(RunSession runSession, RunSegmentInfo data) {
		this.runSession = runSession;
		this.data = data;
	}
}
