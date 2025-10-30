package com.sixpack.dorundorun.infra.naver.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주소 정보 DTO (Redis 캐시용)")
public record AddressInfo(
	@Schema(description = "주소 (시/도 시/구/군 형식)", example = "서울 마포구")
	String address,

	@Schema(description = "캐시 생성 시간 (ISO 8601 형식)", example = "2025-10-30T14:30:00")
	LocalDateTime cachedAt
) {
}
