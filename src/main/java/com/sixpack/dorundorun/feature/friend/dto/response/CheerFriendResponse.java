package com.sixpack.dorundorun.feature.friend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 응원 응답 DTO")
public record CheerFriendResponse(
	@Schema(description = "응원받은 친구의 닉네임", example = "runner123")
	String nickname
) {
}
