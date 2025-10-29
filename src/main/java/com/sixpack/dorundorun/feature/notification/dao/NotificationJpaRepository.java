package com.sixpack.dorundorun.feature.notification.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sixpack.dorundorun.feature.notification.domain.Notification;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {

	// 사용자의 알림 목록 조회 (페이징)
	@Query("SELECT n FROM Notification n WHERE n.userDeviceToken = :deviceToken AND n.deletedAt IS NULL ORDER BY n.createdAt DESC")
	Page<Notification> findByUserDeviceTokenAndDeletedAtIsNull(
		@Param("deviceToken") String deviceToken,
		Pageable pageable
	);

	// 읽지 않은 알림 개수 조회
	@Query("SELECT COUNT(n) FROM Notification n WHERE n.userDeviceToken = :deviceToken AND n.isRead = false AND n.deletedAt IS NULL")
	long countUnreadByUserDeviceToken(@Param("deviceToken") String deviceToken);

	// 특정 알림을 읽음으로 표시
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.id = :notificationId AND n.deletedAt IS NULL")
	void markAsReadById(@Param("notificationId") Long notificationId);

}
