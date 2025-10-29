package com.sixpack.dorundorun.feature.friend.dao.projection;

import java.time.LocalDateTime;

import com.sixpack.dorundorun.feature.run.domain.RunSegmentInfo;

public interface FriendRunningStatusProjection {

	Long getUserId();

	Integer getIsMe();

	String getNickname();

	String getProfileImage();

	LocalDateTime getLatestRanAt();

	// JSON 전체를 가져오기
	RunSegmentInfo getRunSegmentData();

	LocalDateTime getLatestCheeredAt();
}
