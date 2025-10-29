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
			case "CERTIFICATION_UPLOADED" -> "친구의 인증 게시물";
			case "POST_REACTION" -> "게시물 리액션";
			case "POST_COMMENT" -> "게시물 댓글";

			// 스케줄 알림
			case "CERTIFICATION_REMINDER" -> "인증 독촉";
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
			case "CERTIFICATION_UPLOADED" -> buildCertificationUploadedMessage(metadata);
			case "POST_REACTION" -> buildPostReactionMessage(metadata);

			// 스케줄 알림
			case "CERTIFICATION_REMINDER" -> "러닝 인증 마감 1시간 전이에요! 인증을 진행해주세요";
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
			case "CERTIFICATION_UPLOADED" -> "app://certification/" + relatedId;
			case "POST_REACTION" -> "app://post/" + relatedId;
			case "POST_COMMENT" -> "app://post/" + relatedId;

			// 스케줄 알림
			case "CERTIFICATION_REMINDER" -> "app://certification/upload";
			case "RUNNING_PROGRESS_REMINDER" -> "app://running/start";
			case "NEW_USER_RUNNING_REMINDER" -> "app://running/start";
			case "NEW_USER_FRIEND_REMINDER" -> "app://friend/add";

			default -> null;
		};
	}

	private String buildCheerFriendMessage(Map<String, Object> metadata) {
		Object cheererName = metadata.get("cheererName");
		return cheererName != null ?
			cheererName + "님이 회원님을 깨웠어요" :
			"누군가 회원님을 깨웠어요";
	}

	private String buildCertificationUploadedMessage(Map<String, Object> metadata) {
		Object uploaderName = metadata.get("uploaderName");
		return uploaderName != null ?
			uploaderName + "님이 인증 게시물을 업로드 했어요" :
			"친구가 인증 게시물을 업로드 했어요";
	}

	private String buildPostReactionMessage(Map<String, Object> metadata) {
		Object reactorName = metadata.get("reactorName");
		return reactorName != null ?
			reactorName + "님이 회원님의 게시물에 리액션을 남겼어요" :
			"누군가 회원님의 게시물에 리액션을 남겼어요";
	}

	private String getDefaultMessage(String notificationType) {
		return switch (notificationType) {
			case "CERTIFICATION_REMINDER" -> "러닝 인증 마감 1시간 전이에요! 인증을 진행해주세요";
			case "RUNNING_PROGRESS_REMINDER" -> "오랜만에 가볍게 달려볼까요?";
			case "NEW_USER_RUNNING_REMINDER" -> "두런두런과 설레는 첫 러닝을 시작해봐요!";
			case "NEW_USER_FRIEND_REMINDER" -> "친구를 추가하고 멀리서도 함께 러닝을 즐겨보세요!";
			default -> "자세히 확인해보세요";
		};
	}
}
