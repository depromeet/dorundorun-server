package com.sixpack.dorundorun.feature.notification.dto.response;

import java.time.LocalDateTime;

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
	  "message": "김철수님이 당신을 응원합니다!",
	  "type": "CHEER_FRIEND",
	  "isRead": false,
	  "readAt": null,
	  "deepLink": "/user/123",
	  "relatedId": 123,
	  "createdAt": "2025-10-29T14:35:13"
	}
	""")
public class NotificationResponse {

	@Schema(description = "알림 ID", example = "1")
	private Long id;

	@Schema(description = "알림 제목", example = "친구 응원")
	private String title;

	@Schema(description = "알림 메시지", example = "김철수님이 당신을 응원합니다!")
	private String message;

	@Schema(description = "알림 유형 - 8가지 타입 지원:\n" +
		"- CHEER_FRIEND: 친구 응원\n" +
		"- CERTIFICATION_UPLOADED: 친구의 인증게시물 업로드\n" +
		"- POST_REACTION: 게시물 리액션\n" +
		"- POST_COMMENT: 게시물 댓글\n" +
		"- CERTIFICATION_REMINDER: 인증 독촉\n" +
		"- RUNNING_PROGRESS_REMINDER: 러닝 진행 독촉\n" +
		"- NEW_USER_RUNNING_REMINDER: 신규 가입 러닝 독촉\n" +
		"- NEW_USER_FRIEND_REMINDER: 신규 가입 친구추가 독촉",
		example = "CHEER_FRIEND")
	private NotificationType type;

	@Schema(description = "읽음 여부", example = "false")
	private Boolean isRead;

	@Schema(description = "읽은 시간 (읽지 않았을 경우 null)", example = "2025-10-28T14:30:00")
	private LocalDateTime readAt;

	@Schema(description = "딥링크 (알림 클릭 시 이동할 앱 내 경로)", example = "/user/123")
	private String deepLink;

	@Schema(description = "관련 엔티티의 ID (타입별 다름):\n" +
		"- CHEER_FRIEND: friendId\n" +
		"- CERTIFICATION_UPLOADED: certificationId\n" +
		"- POST_REACTION: postId\n" +
		"- POST_COMMENT: postId\n" +
		"- 정보성 알림: 0 또는 null",
		example = "123")
	private Long relatedId;

	@Schema(description = "생성 시간", example = "2025-10-29T14:35:13")
	private LocalDateTime createdAt;

	public static NotificationResponse from(Notification notification) {
		return NotificationResponse.builder()
			.id(notification.getId())
			.title(notification.getData().getTitle())
			.message(notification.getData().getMessage())
			.type(notification.getType())
			.isRead(notification.getIsRead())
			.readAt(notification.getReadAt())
			.deepLink(notification.getDeepLink())
			.relatedId(notification.getData().getAdditionalData() != null ?
				Long.valueOf(notification.getData().getAdditionalData().getOrDefault("relatedId", 0).toString()) :
				null)
			.createdAt(notification.getCreatedAt())
			.build();
	}
}
