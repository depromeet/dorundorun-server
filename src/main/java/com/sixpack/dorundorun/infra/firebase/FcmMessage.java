package com.sixpack.dorundorun.infra.firebase;

import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FCM 메시지 요청")
public record FcmMessage(
	@Schema(description = "디바이스 토큰")
	String deviceToken,

	@Schema(description = "알림 제목")
	String title,

	@Schema(description = "알림 내용")
	String body,

	@Schema(description = "알림 타입 및 관련 데이터")
	NotificationData data,

	@Schema(description = "이미지 URL")
	String imageUrl,

	@Schema(description = "Deep Link (앱 내 특정 화면으로 이동)")
	String deepLink
) {
	@Schema(description = "알림 데이터")
	public record NotificationData(
		@Schema(description = "알림 타입", example = "FRIEND_REQUEST")
		String notificationType,

		@Schema(description = "관련 사용자/리소스 ID")
		String relatedId,

		@Schema(description = "추가 메타데이터")
		Map<String, String> metadata
	) {
	}
}
