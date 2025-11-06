package com.sixpack.dorundorun.feature.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "셀피 반응 응답")
public record SelfieReactionResponse(

	@Schema(description = "셀피 ID", example = "1")
	Long selfieId,

	@Schema(description = "이모지 타입", example = "FIRE")
	String emojiType,

	@Schema(
		description = "수행된 액션",
		example = "ADDED",
		allowableValues = {"ADDED", "REMOVED"}
	)
	ReactionAction action,

	@Schema(description = "해당 셀피의 전체 반응 개수", example = "4")
	Integer totalReactionCount
) {
}
