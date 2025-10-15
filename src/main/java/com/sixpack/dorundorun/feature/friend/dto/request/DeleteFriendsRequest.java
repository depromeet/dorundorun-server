package com.sixpack.dorundorun.feature.friend.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

@Schema(description = "친구 삭제 요청 DTO")
public record DeleteFriendsRequest(
	@Schema(description = "삭제할 친구 ID 목록", example = "[123, 124, 125]")
	@NotEmpty(message = "삭제할 친구 ID 목록은 비어있을 수 없습니다.")
	List<Long> friendIds
) {
}
