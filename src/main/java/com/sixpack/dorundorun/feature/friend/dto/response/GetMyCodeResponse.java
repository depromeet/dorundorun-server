package com.sixpack.dorundorun.feature.friend.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 코드 조회 응답 DTO")
public record GetMyCodeResponse(
	@Schema(description = "내 코드, 숫자와 영어가 섞인 고유코드입니다.", example = "497dcba3")
	String code
) {
}
