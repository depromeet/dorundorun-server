package com.sixpack.dorundorun.feature.attendance.application;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.sixpack.dorundorun.feature.attendance.dao.AttendanceStreakJpaRepository;
import com.sixpack.dorundorun.feature.attendance.domain.AttendanceStreak;
import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.service.ServiceTest;
import com.sixpack.dorundorun.global.utils.KoreaTimeHandler;

@DisplayName("CheckInService 동시성 통합 테스트")
class CheckInConcurrencyIntegrationTest extends ServiceTest {

	private static final int CONCURRENT_REQUESTS = 10;

	@Autowired
	private CheckInService checkInService;

	@Autowired
	private AttendanceStreakJpaRepository attendanceStreakJpaRepository;

	@Autowired
	private UserJpaRepository userJpaRepository;

	@Autowired
	private KoreaTimeHandler koreaTimeHandler;

	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
			.phoneNumber("010-0000-0000")
			.code("ATTEND001")
			.nickname("출석테스터")
			.deviceToken("test-device-token-attendance")
			.build();
		testUser = userJpaRepository.save(testUser);
	}

	@AfterEach
	void tearDown() {
		attendanceStreakJpaRepository.deleteAll();
		userJpaRepository.delete(testUser);
	}

	@Test
	@DisplayName("같은 유저가 동시에 여러 번 체크인해도 스트릭은 정확히 1만 증가한다")
	void checkIn_concurrentRequests_incrementsStreakExactlyOnce() throws InterruptedException {
		LocalDate today = koreaTimeHandler.now();
		attendanceStreakJpaRepository.save(AttendanceStreak.builder()
			.user(testUser)
			.lastCheckinDate(today.minusDays(1))
			.streakCount(4)
			.longestStreak(4)
			.build());

		ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
		CountDownLatch readyLatch = new CountDownLatch(CONCURRENT_REQUESTS);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch doneLatch = new CountDownLatch(CONCURRENT_REQUESTS);

		for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
			executor.submit(() -> {
				readyLatch.countDown();
				try {
					startLatch.await();
					checkInService.checkIn(testUser);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				} finally {
					doneLatch.countDown();
				}
			});
		}

		readyLatch.await();
		startLatch.countDown();
		boolean completed = doneLatch.await(30, TimeUnit.SECONDS);
		executor.shutdown();

		assertThat(completed).isTrue();

		AttendanceStreak result = attendanceStreakJpaRepository.findByUserId(testUser.getId())
			.orElseThrow();
		assertThat(result.getStreakCount()).isEqualTo(5);
		assertThat(result.getLongestStreak()).isEqualTo(5);
		assertThat(result.getLastCheckinDate()).isEqualTo(today);
	}
}
