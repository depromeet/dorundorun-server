package com.sixpack.dorundorun.feature.friend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "친구 추가 요청 DTO")
public record AddFriendRequest(
	@Schema(description = "친구 코드, 숫자와 영어가 섞인 고유코드입니다.", example = "497dcba3")
	@NotBlank(message = "친구 코드는 필수입니다.")
	String code
) {
}
