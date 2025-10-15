package com.sixpack.dorundorun.feature.friend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 추가 응답 DTO")
public record AddFriendResponse(
	@Schema(description = "추가한 친구의 유저 ID", example = "123")
	Long userId
) {
}
