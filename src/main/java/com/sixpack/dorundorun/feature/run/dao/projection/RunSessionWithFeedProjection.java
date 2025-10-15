package com.sixpack.dorundorun.feature.run.dao.projection;

import java.time.LocalDateTime;

public interface RunSessionWithFeedProjection {

	Long getId();

	LocalDateTime getCreatedAt();

	LocalDateTime getUpdatedAt();

	LocalDateTime getFinishedAt();

	Long getDistanceTotal();

	Long getDurationTotal();

	Long getPaceAvg();

	Integer getCadenceAvg();

	Boolean getIsSelfied();
}
