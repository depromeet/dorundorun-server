package com.sixpack.dorundorun.feature.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "셀피 수정 응답")
public record UpdateSelfieResponse(
	@Schema(
		description = "수정된 셀피 이미지 URL (presigned URL). 이미지 삭제 시 null, 이미지 변경 시 새 URL 반환",
		example = "https://s3.amazonaws.com/bucket/selfie123.jpg?X-Amz-Signature=..."
	)
	String selfieImageUrl
) {
}
