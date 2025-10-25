package com.sixpack.dorundorun.feature.feed.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.domain.Reaction;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "셀피 피드 응답")
public record SelfieFeedResponse(

	@Schema(description = "유저 정보 (userId가 있을 때만)")
	UserSummary userSummary,

	@Schema(description = "인증 피드 목록")
	List<FeedItem> feeds
) {
	@Schema(description = "유저 요약 정보")
	public record UserSummary(
		@Schema(description = "유저 이름", example = "닉네임")
		String name,

		@Schema(description = "친구 수", example = "7")
		Integer friendCount,

		@Schema(description = "누적 거리 (m)", example = "400000")
		Long totalDistance,

		@Schema(description = "인증 횟수", example = "120")
		Integer selfieCount
	) {
	}

	@Schema(description = "인증피드 아이템")
	public record FeedItem(
		@Schema(description = "피드 ID", example = "1")
		Long feedId,

		@Schema(description = "날짜", example = "2025-09-20")
		String date,

		@Schema(description = "유저 이름", example = "닉네임")
		String userName,

		@Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
		String profileImageUrl,

		@Schema(description = "인증 시간", example = "2025-09-20T23:58:00")
		LocalDateTime selfieTime,

		@Schema(description = "총 달린 거리 (m)", example = "5100")
		Long totalDistance,

		@Schema(description = "총 달린 시간 (초)", example = "2647")
		Long totalRunTime,

		@Schema(description = "평균 페이스 (초/km)", example = "360")
		Long averagePace,

		@Schema(description = "케이던스 (spm)", example = "144")
		Integer cadence,

		@Schema(description = "인증 이미지 URL", example = "https://example.com/images/selfie123.jpg")
		String imageUrl,

		@Schema(description = "반응 목록")
		List<ReactionSummary> reactions
	) {

		public static FeedItem of(Feed feed, List<ReactionSummary> summaries) {
			return new FeedItem(
				feed.getId(),
				feed.getCreatedAt().toLocalDate().toString(),
				feed.getUser().getNickname(),
				feed.getUser().getProfileImageUrl(),
				feed.getCreatedAt(),
				feed.getRunSession().getDistanceTotal(),
				feed.getRunSession().getDurationTotal(),
				feed.getRunSession().getPaceAvg(),
				feed.getRunSession().getCadenceAvg(),
				feed.getSelfieImageUrl(),
				summaries
			);
		}
	}

	@Schema(description = "반응 요약")
	public record ReactionSummary(
		@Schema(description = "이모지 타입", example = "FIRE")
		String emojiType,

		@Schema(description = "전체 반응 수", example = "5")
		Integer totalCount,

		@Schema(description = "반응한 유저 목록 (전체 포함, 상세 화면에서 사용)")
		List<ReactionUser> users
	) {
	}

	@Schema(description = "반응한 유저 정보")
	public record ReactionUser(
		@Schema(description = "유저 ID", example = "1")
		Long userId,

		@Schema(description = "유저 닉네임", example = "러너123")
		String nickname,

		@Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/profiles/user123.jpg")
		String profileImageUrl,

		@Schema(description = "반응 시간", example = "2025-10-16T14:30:00")
		LocalDateTime reactedAt
	) {

		public static ReactionUser from(Reaction reaction) {
			return new ReactionUser(
				reaction.getUser().getId(),
				reaction.getUser().getNickname(),
				reaction.getUser().getProfileImageUrl(),
				reaction.getCreatedAt()
			);
		}
	}
}
