package com.sixpack.dorundorun.feature.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SMS 인증 확인 요청")
public record SmsVerificationRequest(
	@Schema(description = "전화번호", example = "000-1111-2222")
	String phoneNumber,

	@Schema(description = "인증 코드 (6자리)", example = "000000")
	String verificationCode
) {
}
