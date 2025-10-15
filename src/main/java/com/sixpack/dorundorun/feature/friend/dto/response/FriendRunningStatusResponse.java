package com.sixpack.dorundorun.feature.friend.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 러닝 현황 응답 DTO")
public record FriendRunningStatusResponse(
	@Schema(description = "유저 ID (본인 또는 친구)", example = "123")
	Long userId,

	@Schema(description = "본인 여부", example = "true")
	Boolean isMe,

	@Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
	String profileImage,

	@Schema(description = "최근 러닝 시각 (ISO 8601 형식)", example = "2025-09-13T19:57:13Z")
	LocalDateTime latestRanAt,

	@Schema(description = "최근 러닝의 총 거리 (m)", example = "5000")
	Long distance,

	@Schema(description = "최근 러닝의 위도", example = "37.5301")
	Double latitude,

	@Schema(description = "최근 러닝의 경도", example = "127.12345")
	Double longitude
) {
}
