package com.sixpack.dorundorun.feature.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청")
public record SignUpRequest(
		@NotBlank(message = "이름은 필수입니다.")
		@Size(min = 1, max = 50, message = "이름은 1자 이상 50자 이하여야 합니다.")
		@Schema(description = "사용자 이름", example = "홍길동")
		String name,

		@NotBlank(message = "이메일은 필수입니다.")
		@Email(message = "올바른 이메일 형식이어야 합니다.")
		@Schema(description = "사용자 이메일", example = "user@example.com")
		String email,

		@NotBlank(message = "비밀번호는 필수입니다.")
		@Size(min = 6, max = 20, message = "비밀번호는 6자 이상 20자 이하여야 합니다.")
		@Schema(description = "비밀번호", example = "password123")
		String password
) {
}
