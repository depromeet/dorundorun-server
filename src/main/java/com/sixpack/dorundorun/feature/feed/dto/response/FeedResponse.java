package com.sixpack.dorundorun.feature.feed.dto.response;

import java.time.LocalDateTime;

import com.sixpack.dorundorun.feature.feed.domain.Feed;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "피드 정보")
public record FeedResponse(
	@Schema(description = "피드 ID", example = "1")
	Long id,

	@Schema(description = "맵 이미지 URL", example = "https://example.com/map.jpg")
	String mapImage,

	@Schema(description = "셀피 이미지 URL", example = "https://example.com/selfie.jpg")
	String selfieImage,

	@Schema(description = "피드 내용", example = "오늘 아침 러닝 완주!")
	String content,

	@Schema(description = "피드 생성 일시", example = "2024-01-15T10:30:00.000000Z")
	LocalDateTime createdAt
) {
	public static FeedResponse from(Feed feed) {
		return new FeedResponse(
			feed.getId(),
			feed.getMapImageUrl(),
			feed.getSelfieImageUrl(),
			feed.getContent(),
			feed.getCreatedAt()
		);
	}
}
