package com.sixpack.dorundorun.feature.attendance.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sixpack.dorundorun.feature.attendance.dao.AttendanceStreakJpaRepository;
import com.sixpack.dorundorun.feature.attendance.domain.AttendanceStreak;
import com.sixpack.dorundorun.feature.attendance.dto.response.CheckInResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.utils.KoreaTimeHandler;

@DisplayName("CheckInService 테스트")
class CheckInServiceTest {

	@Mock
	private AttendanceStreakJpaRepository attendanceStreakJpaRepository;

	@Mock
	private KoreaTimeHandler koreaTimeHandler;

	private CheckInService checkInService;

	private User testUser;
	private static final LocalDate TODAY = LocalDate.of(2026, 7, 23);

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		checkInService = new CheckInService(attendanceStreakJpaRepository, koreaTimeHandler);

		testUser = User.builder().id(1L).build();

		when(koreaTimeHandler.now()).thenReturn(TODAY);
	}

	@Test
	@DisplayName("오늘 이미 체크인했다면 스트릭을 변경하지 않고 그대로 응답한다")
	void checkIn_alreadyCheckedInToday_doesNotChangeStreak() {
		AttendanceStreak streak = AttendanceStreak.builder()
			.user(testUser)
			.lastCheckinDate(TODAY)
			.streakCount(5)
			.longestStreak(5)
			.build();
		when(attendanceStreakJpaRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(streak));

		CheckInResponse response = checkInService.checkIn(testUser);

		assertThat(response.streakCount()).isEqualTo(5);
		assertThat(response.longestStreak()).isEqualTo(5);
		assertThat(response.isFirstCheckInToday()).isFalse();
		verify(attendanceStreakJpaRepository, never()).insertIfAbsent(any(), any());
	}

	@Test
	@DisplayName("어제 체크인했다면 스트릭이 1 증가한다")
	void checkIn_checkedInYesterday_incrementsStreak() {
		AttendanceStreak streak = AttendanceStreak.builder()
			.user(testUser)
			.lastCheckinDate(TODAY.minusDays(1))
			.streakCount(4)
			.longestStreak(4)
			.build();
		when(attendanceStreakJpaRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(streak));

		CheckInResponse response = checkInService.checkIn(testUser);

		assertThat(response.streakCount()).isEqualTo(5);
		assertThat(response.longestStreak()).isEqualTo(5);
		assertThat(response.isFirstCheckInToday()).isTrue();
		verify(attendanceStreakJpaRepository, never()).insertIfAbsent(any(), any());
	}

	@Test
	@DisplayName("그저께 이전에 체크인했다면 스트릭이 1로 리셋된다")
	void checkIn_gapInAttendance_resetsStreakToOne() {
		AttendanceStreak streak = AttendanceStreak.builder()
			.user(testUser)
			.lastCheckinDate(TODAY.minusDays(3))
			.streakCount(10)
			.longestStreak(10)
			.build();
		when(attendanceStreakJpaRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(streak));

		CheckInResponse response = checkInService.checkIn(testUser);

		assertThat(response.streakCount()).isEqualTo(1);
		assertThat(response.longestStreak()).isEqualTo(10);
		assertThat(response.isFirstCheckInToday()).isTrue();
		verify(attendanceStreakJpaRepository, never()).insertIfAbsent(any(), any());
	}

	@Test
	@DisplayName("처음 체크인하는 유저는 스트릭 1로 새로 생성된다")
	void checkIn_firstEverCheckIn_createsStreakWithOne() {
		// insertIfAbsent가 만들어낸 직후 상태를 그대로 모킹 (last_checkin_date = 어제, streak/longest = 0)
		AttendanceStreak upsertedStreak = AttendanceStreak.builder()
			.user(testUser)
			.lastCheckinDate(TODAY.minusDays(1))
			.streakCount(0)
			.longestStreak(0)
			.build();
		when(attendanceStreakJpaRepository.findByUserIdForUpdate(1L))
			.thenReturn(Optional.empty())
			.thenReturn(Optional.of(upsertedStreak));

		CheckInResponse response = checkInService.checkIn(testUser);

		verify(attendanceStreakJpaRepository).insertIfAbsent(1L, TODAY.minusDays(1));
		assertThat(response.streakCount()).isEqualTo(1);
		assertThat(response.longestStreak()).isEqualTo(1);
		assertThat(response.isFirstCheckInToday()).isTrue();
	}
}
