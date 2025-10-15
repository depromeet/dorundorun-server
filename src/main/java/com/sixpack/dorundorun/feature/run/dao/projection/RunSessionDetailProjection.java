package com.sixpack.dorundorun.feature.run.dao.projection;

import java.time.LocalDateTime;

public interface RunSessionDetailProjection {
	Long getId();

	LocalDateTime getCreatedAt();

	LocalDateTime getUpdatedAt();

	LocalDateTime getFinishedAt();

	Long getDistanceTotal();

	Long getDurationTotal();

	Long getPaceAvg();

	Long getPaceMax();

	Double getPaceMaxLatitude();

	Double getPaceMaxLongitude();

	Integer getCadenceAvg();

	Integer getCadenceMax();

	Long getFeedId();

	String getFeedMapImage();

	String getFeedSelfieImage();

	String getFeedContent();

	LocalDateTime getFeedCreatedAt();
}
