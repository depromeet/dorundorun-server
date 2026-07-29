package com.sixpack.dorundorun.feature.attendance.application;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.attendance.dao.AttendanceStreakJpaRepository;
import com.sixpack.dorundorun.feature.attendance.domain.AttendanceStreak;
import com.sixpack.dorundorun.feature.attendance.dto.response.CheckInResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.utils.KoreaTimeHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckInService {

	private final AttendanceStreakJpaRepository attendanceStreakJpaRepository;
	private final KoreaTimeHandler koreaTimeHandler;

	// 존재하지 않는 유니크 키에 여러 트랜잭션이 동시에 INSERT ON DUPLICATE KEY UPDATE를 시도할 때
	// REPEATABLE READ의 갭 락으로 인한 데드락을 피하기 위해 READ COMMITTED로 낮춘다.
	@Transactional(isolation = Isolation.READ_COMMITTED)
	public CheckInResponse checkIn(User user) {
		LocalDate today = koreaTimeHandler.now();

		AttendanceStreak streak = attendanceStreakJpaRepository.findByUserIdForUpdate(user.getId())
			.orElseGet(() -> createStreakForFirstEverCheckIn(user, today));

		if (streak.isCheckedInToday(today)) {
			return toResponse(streak, false);
		}

		if (streak.isConsecutive(today)) {
			streak.continueStreak(today);
		} else {
			streak.resetStreak(today);
		}

		return toResponse(streak, true);
	}

	private AttendanceStreak createStreakForFirstEverCheckIn(User user, LocalDate today) {
		attendanceStreakJpaRepository.insertIfAbsent(user.getId(), today.minusDays(1));
		return attendanceStreakJpaRepository.findByUserIdForUpdate(user.getId())
			.orElseThrow(() -> new IllegalStateException(
				"attendance streak row missing after upsert, userId=" + user.getId()));
	}

	private CheckInResponse toResponse(AttendanceStreak streak, boolean isFirstCheckInToday) {
		return new CheckInResponse(streak.getStreakCount(), streak.getLongestStreak(), isFirstCheckInToday);
	}
}
