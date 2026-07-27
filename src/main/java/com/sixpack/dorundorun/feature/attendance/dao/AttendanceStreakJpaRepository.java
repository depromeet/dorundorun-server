package com.sixpack.dorundorun.feature.attendance.dao;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sixpack.dorundorun.feature.attendance.domain.AttendanceStreak;

import jakarta.persistence.LockModeType;

public interface AttendanceStreakJpaRepository extends JpaRepository<AttendanceStreak, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM AttendanceStreak s WHERE s.user.id = :userId")
	Optional<AttendanceStreak> findByUserIdForUpdate(@Param("userId") Long userId);

	Optional<AttendanceStreak> findByUserId(Long userId);

	int deleteByUserId(Long userId);

	@Modifying
	@Query(value = """
		INSERT INTO attendance_streak (user_id, last_checkin_date, streak_count, longest_streak, created_at, updated_at)
		VALUES (:userId, :sentinelDate, 0, 0, NOW(), NOW())
		ON DUPLICATE KEY UPDATE id = id
		""", nativeQuery = true)
	void insertIfAbsent(@Param("userId") Long userId, @Param("sentinelDate") LocalDate sentinelDate);
}
