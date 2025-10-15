package com.sixpack.dorundorun.feature.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "셀피 반응 응답")
public class SelfieReactionResponse {

	@Schema(description = "셀피 ID", example = "1")
	private final Long selfieId;

	@Schema(description = "이모지 타입", example = "FIRE")
	private final String emojiType;

	@Schema(description = "수행된 액션 (ADDED: 반응 추가, REMOVED: 반응 취소)", example = "ADDED")
	private final String action;

	@Schema(description = "해당 셀피의 전체 반응 개수", example = "4")
	private final Integer totalReactionCount;

	public SelfieReactionResponse(Long selfieId, String emojiType, String action, Integer totalReactionCount) {
		this.selfieId = selfieId;
		this.emojiType = emojiType;
		this.action = action;
		this.totalReactionCount = totalReactionCount;
	}
}
