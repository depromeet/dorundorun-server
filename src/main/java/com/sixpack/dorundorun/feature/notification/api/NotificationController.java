package com.sixpack.dorundorun.feature.notification.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sixpack.dorundorun.feature.notification.application.MarkNotificationAsReadService;
import com.sixpack.dorundorun.feature.notification.application.NotificationsService;
import com.sixpack.dorundorun.feature.notification.application.UnreadNotificationCountService;
import com.sixpack.dorundorun.feature.notification.dto.response.NotificationResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

	private final NotificationsService getNotificationsService;
	private final MarkNotificationAsReadService markNotificationAsReadService;
	private final UnreadNotificationCountService unreadNotificationCountService;

	@Override
	@GetMapping
	public DorunResponse<Page<NotificationResponse>> getNotifications(
		@CurrentUser User user,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		log.debug("Getting notifications for user: {}", user.getId());
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<NotificationResponse> notifications = getNotificationsService.getNotifications(user, pageRequest);
		return DorunResponse.success("알림 목록을 조회했습니다", notifications);
	}

	@Override
	@PatchMapping("/{notificationId}/read")
	public DorunResponse<Void> markNotificationAsRead(
		@PathVariable Long notificationId,
		@CurrentUser User user
	) {
		log.debug("Marking notification as read: id={}, userId={}", notificationId, user.getId());
		markNotificationAsReadService.markAsRead(notificationId, user);
		return DorunResponse.success("알림을 읽음으로 표시했습니다");
	}

	@Override
	@GetMapping("/unread-count")
	public DorunResponse<Integer> getUnreadNotificationCount(
		@CurrentUser User user
	) {
		log.debug("Getting unread notification count for user: {}", user.getId());
		int count = unreadNotificationCountService.getUnreadCount(user);
		return DorunResponse.success("읽지 않은 알림 개수를 조회했습니다", count);
	}
}
