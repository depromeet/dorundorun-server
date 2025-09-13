package com.sixpack.dorundorun.feature.run.domain;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

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
	private List<RunSegmentData> data;

	@Builder
	public RunSegment(RunSession runSession, List<RunSegmentData> data) {
		this.runSession = runSession;
		this.data = data;
	}
}
