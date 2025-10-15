package com.sixpack.dorundorun.feature.friend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "친구 응원하기 요청 DTO")
public record CheerFriendRequest(
	@Schema(description = "응원할 친구의 유저 ID", example = "123")
	@NotNull(message = "유저 ID는 필수입니다.")
	Long userId
) {
}
