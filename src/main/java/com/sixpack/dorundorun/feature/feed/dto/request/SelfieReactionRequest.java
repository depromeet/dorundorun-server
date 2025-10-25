package com.sixpack.dorundorun.feature.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "셀피 반응 요청")
public record SelfieReactionRequest(

	@Schema(description = "이모지 타입 (예: FIRE, CLAP, HEART 등)", example = "FIRE")
	@NotNull(message = "이모지 타입은 필수입니다.")
	String emojiType
) {
}
