package com.sixpack.dorundorun.feature.attendance.domain;

import java.time.LocalDate;

import com.sixpack.dorundorun.feature.common.model.BaseTimeEntity;
import com.sixpack.dorundorun.feature.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attendance_streak", uniqueConstraints = {
	@UniqueConstraint(name = "uk_attendance_streak_user", columnNames = {"user_id"})
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class AttendanceStreak extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "last_checkin_date", nullable = false)
	private LocalDate lastCheckinDate;

	@Column(name = "streak_count", nullable = false)
	private int streakCount;

	@Column(name = "longest_streak", nullable = false)
	private int longestStreak;

	public boolean isCheckedInToday(LocalDate today) {
		return today.equals(lastCheckinDate);
	}

	public boolean isConsecutive(LocalDate today) {
		return lastCheckinDate.equals(today.minusDays(1));
	}

	public void continueStreak(LocalDate today) {
		this.streakCount += 1;
		this.lastCheckinDate = today;
		if (this.streakCount > this.longestStreak) {
			this.longestStreak = this.streakCount;
		}
	}

	public void resetStreak(LocalDate today) {
		this.streakCount = 1;
		this.lastCheckinDate = today;
		if (this.longestStreak < 1) {
			this.longestStreak = 1;
		}
	}
}
