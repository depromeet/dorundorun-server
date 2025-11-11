package com.sixpack.dorundorun.feature.notification.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

import com.sixpack.dorundorun.feature.notification.domain.Notification;
import com.sixpack.dorundorun.feature.notification.domain.NotificationType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "알림 응답",
	example = """
		{
		  "id": 1,
		  "title": "친구 응원",
		  "message": "이 당신을 응원합니다!",
		  "sender": "김철수",
		  "profileImage": "https://api.dorundorun.store/api/images/defaultProfileImage.jpg",
		  "type": "CHEER_FRIEND",
		  "isRead": false,
		  "readAt": null,
		  "deepLink": "/user/123",
		  "relatedId": 123,
		  "selfieImage": null,
		  "createdAt": "2025-10-29T14:35:13"
		}
		""")
public class NotificationResponse {

	@Schema(description = "알림 ID", example = "1")
	private Long id;

	@Schema(description = "알림 제목", example = "친구 응원")
	private String title;

	@Schema(description = "알림 메시지 (닉네임 제외)", example = "님이 당신을 응원합니다!")
	private String message;

	@Schema(description = "발신자 닉네임", example = "김철수")
	private String sender;

	@Schema(description = "발신자 프로필 이미지 (완전한 HTTPS URL 또는 S3 Presigned URL)", example = "https://api.dorundorun.store/api/images/defaultProfileImage.jpg")
	private String profileImage;

	@Schema(description = "알림 유형 - 7가지 타입 지원:\n" +
		"- CHEER_FRIEND: 친구 응원\n" +
		"- FEED_UPLOADED: 친구의 피드 업로드\n" +
		"- FEED_REACTION: 피드 리액션\n" +
		"- FEED_REMINDER: 피드 업로드 독촉\n" +
		"- RUNNING_PROGRESS_REMINDER: 러닝 진행 독촉\n" +
		"- NEW_USER_RUNNING_REMINDER: 신규 가입 러닝 독촉\n" +
		"- NEW_USER_FRIEND_REMINDER: 신규 가입 친구추가 독촉",
		example = "CHEER_FRIEND")
	private NotificationType type;

	@Schema(description = "읽음 여부", example = "false")
	private Boolean isRead;

	@Schema(description = "읽은 시간 (읽지 않았을 경우 null)", example = "2025-10-28T14:30:00.000000Z")
	private LocalDateTime readAt;

	@Schema(description = "딥링크 (알림 클릭 시 이동할 앱 내 경로)", example = "/user/123")
	private String deepLink;

	@Schema(description = "관련 엔티티의 ID (타입별 다름):\n" +
		"- CHEER_FRIEND: friendId\n" +
		"- FEED_UPLOADED: feedId\n" +
		"- FEED_REACTION: feedId\n" +
		"- 정보성 알림: 0 또는 null",
		example = "123")
	private Long relatedId;

	@Schema(description = "셀피 이미지 URL (FEED_UPLOADED, FEED_REACTION 타입일 때만 S3 Presigned URL 반환, 나머지는 null)", example = "https://s3.amazonaws.com/...")
	private String selfieImage;

	@Schema(description = "생성 시간", example = "2025-10-29T14:35:13.000000Z")
	private LocalDateTime createdAt;

	public static NotificationResponse from(Notification notification) {
		String message = notification.getData().getMessage();
		String sender = getSenderName(notification);

		// 응답에서는 메시지에서 발신자 이름과 "님" 제거
		if (sender != null && message != null) {
			message = message.replace(sender + "님", "");
		}

		return NotificationResponse.builder()
			.id(notification.getId())
			.title(notification.getData().getTitle())
			.message(message)
			.sender(sender)
			.profileImage("/api/images/defaultProfileImage.jpg")
			.type(notification.getType())
			.isRead(notification.getIsRead())
			.readAt(notification.getReadAt())
			.deepLink(notification.getDeepLink())
			.relatedId(notification.getData().getAdditionalData() != null ?
				Long.valueOf(notification.getData().getAdditionalData().getOrDefault("relatedId", 0).toString()) :
				null)
			.selfieImage(null)
			.createdAt(notification.getCreatedAt())
			.build();
	}

	private static String getSenderName(Notification notification) {
		if (notification.getData().getAdditionalData() == null) {
			return null;
		}

		Map<String, Object> additionalData = notification.getData().getAdditionalData();

		// 알림 타입별로 해당하는 발신자명 필드 조회
		if (additionalData.containsKey("cheererName")) {
			return additionalData.get("cheererName").toString();
		} else if (additionalData.containsKey("uploaderName")) {
			return additionalData.get("uploaderName").toString();
		} else if (additionalData.containsKey("reactorName")) {
			return additionalData.get("reactorName").toString();
		}

		return null;
	}

	public static NotificationResponse from(Notification notification, String profileImage, String selfieImage) {
		String message = notification.getData().getMessage();
		String sender = getSenderName(notification);

		// 응답에서는 메시지에서 발신자 이름과 "님" 제거
		if (sender != null && message != null) {
			message = message.replace(sender + "님", "");
		}

		return NotificationResponse.builder()
			.id(notification.getId())
			.title(notification.getData().getTitle())
			.message(message)
			.sender(sender)
			.profileImage(profileImage)
			.type(notification.getType())
			.isRead(notification.getIsRead())
			.readAt(notification.getReadAt())
			.deepLink(notification.getDeepLink())
			.relatedId(notification.getData().getAdditionalData() != null ?
				Long.valueOf(notification.getData().getAdditionalData().getOrDefault("relatedId", 0).toString()) :
				null)
			.selfieImage(selfieImage)
			.createdAt(notification.getCreatedAt())
			.build();
	}
}
