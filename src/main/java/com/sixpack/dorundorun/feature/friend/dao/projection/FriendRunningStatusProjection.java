package com.sixpack.dorundorun.feature.friend.dao.projection;

import java.time.LocalDateTime;

public interface FriendRunningStatusProjection {

	Long getUserId();

	Integer getIsMe();

	String getNickname();

	String getProfileImage();

	LocalDateTime getLatestRanAt();

	Long getDistance();

	Double getLatitude();

	Double getLongitude();
}
