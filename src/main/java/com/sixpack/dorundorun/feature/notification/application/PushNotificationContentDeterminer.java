package com.sixpack.dorundorun.feature.notification.application;

import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PushNotificationContentDeterminer {

	public String determineTitle(String notificationType, Map<String, Object> metadata) {
		return switch (notificationType) {
			// 즉시 알림
			case "CHEER_FRIEND" -> "누군가가 당신을 깨웠어요!";
			case "FEED_UPLOADED" -> "친구의 새 인증이 도착했어요!";
			case "FEED_REACTION" -> "새 리액션이 도착했어요!";

			// 스케줄 알림
			case "FEED_REMINDER" -> "러닝 인증 마감 임박!";
			case "RUNNING_PROGRESS_REMINDER" -> "오늘은 러닝하기 딱 좋은 날!";
			case "NEW_USER_RUNNING_REMINDER" -> "첫 러닝을 시작해볼 시간이에요";
			case "NEW_USER_FRIEND_REMINDER" -> "함께 달릴 친구를 만들어보세요";

			default -> "새로운 알림";
		};
	}

	public String determineMessage(String notificationType, Map<String, Object> metadata) {
		if (metadata == null || metadata.isEmpty()) {
			return getDefaultMessage(notificationType);
		}

		return switch (notificationType) {
			// 즉시 알림
			case "CHEER_FRIEND" -> buildCheerFriendMessage(metadata);
			case "FEED_UPLOADED" -> buildFeedUploadedMessage(metadata);
			case "FEED_REACTION" -> buildFeedReactionMessage(metadata);

			// 스케줄 알림
			case "FEED_REMINDER" -> "러닝 인증 마감 1시간 전이에요! 인증을 진행해주세요.";
			case "RUNNING_PROGRESS_REMINDER" -> "오랜만에 가볍게 달려볼까요?";
			case "NEW_USER_RUNNING_REMINDER" -> "두런두런과 설레는 첫 러닝을 시작해봐요!";
			case "NEW_USER_FRIEND_REMINDER" -> "친구를 추가하고 멀리서도 함께 러닝을 즐겨보세요!";

			default -> getDefaultMessage(notificationType);
		};
	}

	public String determineDeepLink(String notificationType, String relatedId, Map<String, Object> metadata) {
		return switch (notificationType) {
			// 즉시 알림
			case "CHEER_FRIEND" -> "dorundorun://friend/profile/" + relatedId;
			case "FEED_UPLOADED" -> "dorundorun://feed/" + relatedId;
			case "FEED_REACTION" -> "dorundorun://feed/" + relatedId;

			// 스케줄 알림
			case "FEED_REMINDER" -> "dorundorun://feed/upload";
			case "RUNNING_PROGRESS_REMINDER" -> "dorundorun://running/start";
			case "NEW_USER_RUNNING_REMINDER" -> "dorundorun://running/start";
			case "NEW_USER_FRIEND_REMINDER" -> "dorundorun://friend/add";

			default -> null;
		};
	}

	private String buildCheerFriendMessage(Map<String, Object> metadata) {
		String cheererName = (String)metadata.getOrDefault("cheererName", "친구");
		return cheererName + "님이 회원님을 깨웠어요";
	}

	private String buildFeedUploadedMessage(Map<String, Object> metadata) {
		String uploaderName = (String)metadata.getOrDefault("uploaderName", "친구");
		return uploaderName + "님이 피드를 업로드 했어요";
	}

	private String buildFeedReactionMessage(Map<String, Object> metadata) {
		String reactionUserName = (String)metadata.getOrDefault("reactionUserName", "친구");
		return reactionUserName + "님이 회원님의 피드에 리액션을 남겼어요";
	}

	private String getDefaultMessage(String notificationType) {
		return switch (notificationType) {
			case "FEED_REMINDER" -> "러닝 인증 마감 1시간 전이에요! 인증을 진행해주세요.";
			case "RUNNING_PROGRESS_REMINDER" -> "오랜만에 가볍게 달려볼까요?";
			case "NEW_USER_RUNNING_REMINDER" -> "두런두런과 설레는 첫 러닝을 시작해봐요!";
			case "NEW_USER_FRIEND_REMINDER" -> "친구를 추가하고 멀리서도 함께 러닝을 즐겨보세요!";
			default -> "자세히 확인해보세요";
		};
	}
}
