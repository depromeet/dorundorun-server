package com.sixpack.dorundorun.feature.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "셀피 ID 응답")
public class SelfieIdResponse {

	@Schema(description = "셀피 ID", example = "1")
	private final Long selfieId;

	public SelfieIdResponse(Long selfieId) {
		this.selfieId = selfieId;
	}
}
