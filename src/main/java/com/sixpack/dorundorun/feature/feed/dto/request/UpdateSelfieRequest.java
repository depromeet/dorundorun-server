package com.sixpack.dorundorun.feature.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "셀피 수정 요청")
public record UpdateSelfieRequest(
	@Schema(description = "피드 내용", example = "오늘도 완주! 🏃‍♂️")
	String content,

	@Schema(
		description = "셀피 이미지 삭제 여부 (true: 기존 이미지 삭제, false 또는 null: 기존 이미지 유지 또는 새 이미지로 교체)",
		example = "false"
	)
	Boolean deleteSelfieImage
) {
}
