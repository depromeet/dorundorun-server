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
			case "CHEER_FRIEND" -> "깨우기 알림";
			case "FEED_UPLOADED" -> "친구의 피드 업로드";
			case "FEED_REACTION" -> "피드 리액션";

			// 스케줄 알림
			case "FEED_REMINDER" -> "피드 업로드 독촉";
			case "RUNNING_PROGRESS_REMINDER" -> "러닝 독촉";
			case "NEW_USER_RUNNING_REMINDER" -> "러닝 시작";
			case "NEW_USER_FRIEND_REMINDER" -> "친구 추가";

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
			case "FEED_REMINDER" -> "피드를 아직 업로드하지 않으셨나요? 피드를 업로드하고 친구들과 함께해요!";
			case "RUNNING_PROGRESS_REMINDER" -> "오랜만에 가볍게 달려볼까요?";
			case "NEW_USER_RUNNING_REMINDER" -> "두런두런과 설레는 첫 러닝을 시작해봐요!";
			case "NEW_USER_FRIEND_REMINDER" -> "친구를 추가하고 멀리서도 함께 러닝을 즐겨보세요!";

			default -> getDefaultMessage(notificationType);
		};
	}

	public String determineDeepLink(String notificationType, String relatedId, Map<String, Object> metadata) {
		return switch (notificationType) {
			// 즉시 알림
			case "CHEER_FRIEND" -> "app://friend/profile/" + relatedId;
			case "FEED_UPLOADED" -> "app://feed/" + relatedId;
			case "FEED_REACTION" -> "app://feed/" + relatedId;

			// 스케줄 알림
			case "FEED_REMINDER" -> "app://feed/upload";
			case "RUNNING_PROGRESS_REMINDER" -> "app://running/start";
			case "NEW_USER_RUNNING_REMINDER" -> "app://running/start";
			case "NEW_USER_FRIEND_REMINDER" -> "app://friend/add";

			default -> null;
		};
	}

	private String buildCheerFriendMessage(Map<String, Object> metadata) {
		return "님이 회원님을 깨웠어요";
	}

	private String buildFeedUploadedMessage(Map<String, Object> metadata) {
		return "님이 피드를 업로드 했어요";
	}

	private String buildFeedReactionMessage(Map<String, Object> metadata) {
		return "님이 회원님의 피드에 리액션을 남겼어요";
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
