package com.sixpack.dorundorun.feature.run.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.sixpack.dorundorun.feature.feed.dto.response.FeedResponse;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "러닝 세션 상세 조회 응답 DTO")
public record RunSessionDetailResponse(
	@Schema(description = "세션 고유 ID", example = "1")
	Long id,

	@Schema(description = "생성 일시", example = "2024-01-15T09:00:00")
	LocalDateTime createdAt,

	@Schema(description = "수정 일시", example = "2024-01-15T10:30:00")
	LocalDateTime updatedAt,

	@Schema(description = "종료된 일시", example = "2024-01-15T10:30:00")
	LocalDateTime finishedAt,

	@Schema(description = "총 거리 (m 단위)", example = "5000")
	Long distanceTotal,

	@Schema(description = "총 시간 (초 단위)", example = "1800")
	Long durationTotal,

	@Schema(description = "평균 페이스 (초/km)", example = "360")
	Long paceAvg,

	@Schema(description = "최대 페이스 (초/km)", example = "360")
	Long paceMax,

	@Schema(description = "최대 페이스 위도", example = "37.5301")
	Double paceMaxLatitude,

	@Schema(description = "최대 페이스 경도", example = "127.12345")
	Double paceMaxLongitude,

	@Schema(description = "평균 케이던스 (걸음/분)", example = "170")
	Integer cadenceAvg,

	@Schema(description = "최대 케이던스 (걸음/분)", example = "185")
	Integer cadenceMax,

	@Schema(description = "맵 이미지 URL", example = "https://example.com/map.jpg")
	String mapImage,

	@Schema(description = "인증한 피드 정보 (없으면 null)")
	FeedResponse feed,

	@Schema(description = "러닝 세그먼트 목록")
	List<List<RunSegmentData>> segments
) {
}
