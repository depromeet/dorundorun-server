package com.sixpack.dorundorun.feature.friend.dto.response;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "친구 삭제 응답 DTO")
public record DeleteFriendsResponse(
	@Schema(description = "삭제된 친구 ID와 닉네임 매핑", example = "{\"123\": \"runner123\", \"124\": \"runner124\"}")
	Map<Long, String> deletedFriends
) {
}
