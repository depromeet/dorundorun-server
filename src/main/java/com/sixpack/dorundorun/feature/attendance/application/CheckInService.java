package com.sixpack.dorundorun.feature.attendance.application;

import java.time.LocalDate;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
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

	@Transactional
	public CheckInResponse checkIn(User user) {
		LocalDate today = koreaTimeHandler.now();

		AttendanceStreak streak = attendanceStreakJpaRepository.findByUserIdForUpdate(user.getId())
			.orElseGet(() -> createInitialStreak(user));

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

	private AttendanceStreak createInitialStreak(User user) {
		try {
			return attendanceStreakJpaRepository.save(AttendanceStreak.builder()
				.user(user)
				.lastCheckinDate(LocalDate.MIN)
				.streakCount(0)
				.longestStreak(0)
				.build());
		} catch (DataIntegrityViolationException e) {
			return attendanceStreakJpaRepository.findByUserIdForUpdate(user.getId())
				.orElseThrow(() -> e);
		}
	}

	private CheckInResponse toResponse(AttendanceStreak streak, boolean isFirstCheckInToday) {
		return new CheckInResponse(streak.getStreakCount(), streak.getLongestStreak(), isFirstCheckInToday);
	}
}
