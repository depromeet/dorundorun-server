package com.sixpack.dorundorun.feature.user.dto.response;

import com.sixpack.dorundorun.feature.user.domain.User;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 정보 응답")
public record UserResponse(
	@Schema(description = "사용자 ID", example = "1")
	Long id,

	@Schema(description = "사용자 닉네임", example = "러너123")
	String nickname,

	@Schema(description = "디바이스 토큰", example = "device-token-xyz")
	String deviceToken
) {
	public static UserResponse of(User user) {
		return new UserResponse(
			user.getId(),
			user.getNickname(),
			user.getDeviceToken()
		);
	}
}
