package com.sixpack.dorundorun.feature.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "SMS 인증 요청")
public record SmsSendRequest(
	@Schema(description = "전화번호", example = "000-1111-2222")
	String phoneNumber
) {
}
