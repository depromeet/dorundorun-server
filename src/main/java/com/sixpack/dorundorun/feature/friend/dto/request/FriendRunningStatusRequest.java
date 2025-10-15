package com.sixpack.dorundorun.feature.friend.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

@Schema(description = "친구 러닝 현황 조회 요청 DTO")
public record FriendRunningStatusRequest(
	@Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
	@Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다.")
	Integer page,

	@Schema(description = "페이지 크기", example = "20", defaultValue = "20")
	@Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
	Integer size
) {
	public FriendRunningStatusRequest {
		if (page == null) {
			page = 0;
		}
		if (size == null) {
			size = 20;
		}
	}
}
