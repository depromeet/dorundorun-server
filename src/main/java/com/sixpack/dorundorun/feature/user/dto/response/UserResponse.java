package com.sixpack.dorundorun.feature.user.dto.response;

import com.sixpack.dorundorun.feature.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용자 정보 응답")
public record UserResponse(
		@Schema(description = "사용자 ID", example = "1")
		Long id,

		@Schema(description = "사용자 이름", example = "홍길동")
		String name,

		@Schema(description = "사용자 이메일", example = "user@example.com")
		String email
) {
	public static UserResponse of(User user) {
		return new UserResponse(user.getId(), user.getName(), user.getEmail());
	}
}
