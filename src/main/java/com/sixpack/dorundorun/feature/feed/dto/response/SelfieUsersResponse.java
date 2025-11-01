package com.sixpack.dorundorun.feature.feed.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "특정 날짜 셀피 유저 목록 응답")
public record SelfieUsersResponse(
	@Schema(description = "셀피를 올린 유저 목록")
	List<PostingUserState> users
) {

	@Schema(description = "셀피 유저 정보")
	public record PostingUserState(
		@Schema(description = "유저 ID", example = "1")
		Long userId,

		@Schema(description = "유저 이름", example = "러너123")
		String userName,

		@Schema(description = "유저 프로필 이미지 URL", example = "https://example.com/profile.jpg")
		String userImageUrl,

		@Schema(description = "셀피 업로드 시간", example = "2025-10-16T14:30:00")
		LocalDateTime postingTime,

		@Schema(description = "내 셀피 여부", example = "true")
		Boolean isMe
	) {
	}
}
