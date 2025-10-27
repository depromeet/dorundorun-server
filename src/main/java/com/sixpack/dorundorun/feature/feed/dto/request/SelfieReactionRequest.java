package com.sixpack.dorundorun.feature.feed.dto.request;

import com.sixpack.dorundorun.feature.feed.domain.EmojiType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "셀피 반응 요청")
public record SelfieReactionRequest(

	@Schema(description = "피드 ID", example = "123")
	@NotNull(message = "피드 ID는 필수입니다.")
	Long feedId,

	@Schema(description = "이모지 타입 (예: FIRE, SURPRISE, HEART 등)", example = "FIRE")
	@NotNull(message = "이모지 타입은 필수입니다.")
	EmojiType emojiType
) {
}
