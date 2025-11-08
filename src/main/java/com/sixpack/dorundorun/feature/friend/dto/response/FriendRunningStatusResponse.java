package com.sixpack.dorundorun.feature.friend.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 러닝 현황 응답 DTO")
public record FriendRunningStatusResponse(
	@Schema(description = "유저 ID (본인 또는 친구)", example = "123")
	Long userId,

	@Schema(description = "본인 여부", example = "true")
	Boolean isMe,

	@Schema(description = "닉네임", example = "두런이")
	String nickname,

	@Schema(description = "프로필 이미지 URL (앞에 https://api.dorundorun.store 를 붙이면 이미지 조회가 됩니다.)", example = "https://api.dorundorun.store/api/images/defaultProfileImage.jpg")
	String profileImage,

	@Schema(description = "최근 러닝 시각 (ISO 8601 형식)", example = "2025-09-13T19:57:13.000000Z")
	LocalDateTime latestRanAt,

	@Schema(description = "로그인한 유저가 마지막으로 해당 친구를 응원한 시각 (ISO 8601 형식, null 가능)", example = "2025-09-13T19:57:13.000000Z")
	LocalDateTime latestCheeredAt,

	@Schema(description = "최근 러닝의 총 거리 (m)", example = "5000")
	Long distance,

	@Schema(description = "최근 러닝의 위도", example = "37.5301")
	Double latitude,

	@Schema(description = "최근 러닝의 경도", example = "127.12345")
	Double longitude,

	@Schema(description = "주소 (예: 서울 마포구, 경기 광명시)", example = "서울 마포구")
	String address
) {
}
