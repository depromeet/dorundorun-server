package com.sixpack.dorundorun.feature.feed.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "셀피 피드 응답")
public class SelfieFeedResponse {

	@Schema(description = "유저 정보 (userId가 있을 때만)")
	private final UserSummary userSummary;

	@Schema(description = "인증 피드 목록")
	private final List<FeedItem> feeds;

	public SelfieFeedResponse(UserSummary userSummary, List<FeedItem> feeds) {
		this.userSummary = userSummary;
		this.feeds = feeds;
	}

	@Getter
	@Schema(description = "유저 요약 정보")
	public static class UserSummary {
		@Schema(description = "유저 이름", example = "닉네임")
		private final String name;

		@Schema(description = "친구 수", example = "7")
		private final Integer friendCount;

		@Schema(description = "누적 거리 (m)", example = "40000")
		private final Double totalDistance;

		@Schema(description = "인증 횟수", example = "120")
		private final Integer selfieCount;

		public UserSummary(String name, Integer friendCount, Double totalDistance, Integer selfieCount) {
			this.name = name;
			this.friendCount = friendCount;
			this.totalDistance = totalDistance;
			this.selfieCount = selfieCount;
		}
	}

	@Getter
	@Schema(description = "인증피드 아이템")
	public static class FeedItem {
		@Schema(description = "피드 ID", example = "1")
		private final Long feedId;

		@Schema(description = "날짜", example = "2025-09-20")
		private final String date;

		@Schema(description = "유저 이름", example = "닉네임")
		private final String userName;

		@Schema(description = "인증 시간", example = "2025-09-20T23:58:00")
		private final LocalDateTime selfieTime;

		@Schema(description = "총 달린 거리 (m)", example = "5.10")
		private final Double totalDistance;

		@Schema(description = "총 달린 시간 (초)", example = "2647")
		private final Integer totalRunTime;

		@Schema(description = "평균 페이스 (초/km)", example = "360")
		private final String averagePace;

		@Schema(description = "케이던스 (spm)", example = "144")
		private final Integer cadence;

		@Schema(description = "인증 이미지 URL", example = "https://example.com/images/selfie123.jpg")
		private final String imageUrl;

		@Schema(description = "반응 목록")
		private final List<ReactionSummary> reactions;

		public FeedItem(Long feedId, String date, String userName, LocalDateTime selfieTime,
			Double totalDistance, Integer totalRunTime, String averagePace, Integer cadence,
			String imageUrl, List<ReactionSummary> reactions) {
			this.feedId = feedId;
			this.date = date;
			this.userName = userName;
			this.selfieTime = selfieTime;
			this.totalDistance = totalDistance;
			this.totalRunTime = totalRunTime;
			this.averagePace = averagePace;
			this.cadence = cadence;
			this.imageUrl = imageUrl;
			this.reactions = reactions;
		}
	}

	@Getter
	@Schema(description = "반응 요약")
	public static class ReactionSummary {
		@Schema(description = "이모지 타입", example = "FIRE")
		private final String emojiType;

		@Schema(description = "반응 수", example = "3")
		private final Integer count;

		public ReactionSummary(String emojiType, Integer count) {
			this.emojiType = emojiType;
			this.count = count;
		}
	}
}
