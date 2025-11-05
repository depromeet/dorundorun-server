package com.sixpack.dorundorun.feature.notification.api;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestParam;

import com.sixpack.dorundorun.feature.notification.dto.response.NotificationResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[알림 관련]")
public interface NotificationApi {

	@Operation(summary = "사용자의 알림 목록 조회",
		description = "사용자가 받은 알림 목록을 최신순으로 조회합니다.\n\n" +
			"알림 타입별 Deeplink 경로:\n" +
			"- CHEER_FRIEND: 친구 응원 → dorundorun://friend/profile/{relatedId = friendId} (응원한 친구의 프로필)\n" +
			"- FEED_UPLOADED: 친구의 피드 업로드 → dorundorun://feed/{relatedId = feedId} (업로드된 피드 상세 보기)\n" +
			"- FEED_REACTION: 피드 리액션 → dorundorun://feed/{relatedId = feedId} (리액션이 달린 피드 상세 보기)\n" +
			"- FEED_REMINDER: 피드 업로드 독촉 → dorundorun://feed/upload (피드 작성 화면)\n" +
			"- RUNNING_PROGRESS_REMINDER: 러닝 진행 독촉 → dorundorun://running/start (러닝 시작 화면)\n" +
			"- NEW_USER_RUNNING_REMINDER: 신규 가입 러닝 독촉 → dorundorun://running/start (러닝 시작 화면)\n" +
			"- NEW_USER_FRIEND_REMINDER: 신규 가입 친구추가 독촉 → dorundorun://friend/add (친구 추가 화면)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "알림 목록 조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	DorunResponse<Page<NotificationResponse>> getNotifications(
		@Parameter(hidden = true) @CurrentUser User user,
		@Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
		@Parameter(description = "페이지 크기", example = "10") @RequestParam(defaultValue = "10") int size
	);

	@Operation(summary = "알림을 읽음으로 표시",
		description = "특정 알림을 읽음 상태로 변경합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "알림 읽음 처리 성공"),
		@ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음"),
		@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	DorunResponse<Void> markNotificationAsRead(
		@Parameter(description = "알림 ID", required = true) Long notificationId,
		@Parameter(hidden = true) @CurrentUser User user
	);

	@Operation(summary = "읽지 않은 알림 개수 조회",
		description = "사용자가 읽지 않은 알림의 개수를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "읽지 않은 알림 개수 조회 성공"),
		@ApiResponse(responseCode = "401", description = "인증 필요")
	})
	DorunResponse<Integer> getUnreadNotificationCount(
		@Parameter(hidden = true) @CurrentUser User user
	);
}
