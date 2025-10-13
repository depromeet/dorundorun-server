package com.sixpack.dorundorun.feature.notification.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sixpack.dorundorun.feature.notification.domain.Notification;

public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {
}
