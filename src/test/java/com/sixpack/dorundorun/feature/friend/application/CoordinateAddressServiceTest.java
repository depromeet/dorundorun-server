package com.sixpack.dorundorun.feature.friend.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.sixpack.dorundorun.feature.friend.dao.projection.FriendRunningStatusProjection;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentInfo;
import com.sixpack.dorundorun.infra.naver.dto.AddressInfo;

@SpringBootTest
@DisplayName("CoordinateAddressService 성능 비교 테스트")
class CoordinateAddressServiceTest {

	private static final Logger log = LoggerFactory.getLogger(CoordinateAddressServiceTest.class);

	@Autowired
	private CoordinateAddressService coordinateAddressService;

	@MockBean
	private ReverseGeocodingService reverseGeocodingService;

	@BeforeEach
	void setUp() {
		// API 호출 시 100ms 지연을 시뮬레이션
		when(reverseGeocodingService.addressByCoordinates(anyDouble(), anyDouble()))
			.thenAnswer(invocation -> {
				Thread.sleep(100);  // 100ms 지연
				Double lat = invocation.getArgument(0);
				Double lon = invocation.getArgument(1);
				return new AddressInfo("Test Address (" + lat + "," + lon + ")", LocalDateTime.now());
			});
	}

	@Test
	@DisplayName("병렬 처리 vs 순차 처리 성능 비교 - 기본 시나리오 (친구 5명, API 지연 100ms)")
	void performanceComparison_Basic() {
		// 테스트 데이터 준비 (친구 5명, 각 100ms 지연 시뮬레이션)
		List<FriendRunningStatusProjection> friends = createMockFriends(5);

		log.info("\n========================================");
		log.info("성능 비교 테스트: 기본 시나리오");
		log.info("친구 수: {}", friends.size());
		log.info("API 응답 시간: 100ms (mock)");
		log.info("최대 동시 요청: 5");
		log.info("========================================\n");

		// 1. 병렬 처리 성능 측정 (워밍업)
		log.info("[WarmUp] 병렬 처리 워밍업 실행 중...");
		coordinateAddressService.extractAndConvertCoordinatesWithCoordinates(friends);

		// 2. 본격 병렬 처리 성능 측정 (5회 반복)
		log.info("[1] 병렬 처리 (CompletableFuture.allOf) 실행 중 (5회 반복 측정)...");
		long[] parallelTimes = new long[5];
		for (int i = 0; i < 5; i++) {
			long start = System.nanoTime();
			var result = coordinateAddressService.extractAndConvertCoordinatesWithCoordinates(friends);
			long end = System.nanoTime();
			parallelTimes[i] = (end - start) / 1_000_000;
			log.info("   - 반복 {}: {}ms", i + 1, parallelTimes[i]);
		}
		long parallelAvg = (long) java.util.Arrays.stream(parallelTimes).average().orElse(0);
		long parallelMin = java.util.Arrays.stream(parallelTimes).min().orElse(0);
		long parallelMax = java.util.Arrays.stream(parallelTimes).max().orElse(0);
		log.info("✓ 병렬 처리 완료");

		// 3. 순차 처리 성능 측정 (5회 반복)
		log.info("[2] 순차 처리 (Sequential) 실행 중 (5회 반복 측정)...");
		long[] sequentialTimes = new long[5];
		for (int i = 0; i < 5; i++) {
			long start = System.nanoTime();
			var result = coordinateAddressService.extractAndConvertCoordinatesSequential(friends);
			long end = System.nanoTime();
			sequentialTimes[i] = (end - start) / 1_000_000;
			log.info("   - 반복 {}: {}ms", i + 1, sequentialTimes[i]);
		}
		long sequentialAvg = (long) java.util.Arrays.stream(sequentialTimes).average().orElse(0);
		long sequentialMin = java.util.Arrays.stream(sequentialTimes).min().orElse(0);
		long sequentialMax = java.util.Arrays.stream(sequentialTimes).max().orElse(0);
		log.info("✓ 순차 처리 완료");

		// 4. 결과 비교
		double improvementRatio = sequentialAvg / (double)parallelAvg;
		long timeSaved = sequentialAvg - parallelAvg;

		log.info("\n========================================");
		log.info("성능 비교 결과 (5회 반복 측정)");
		log.info("========================================");
		log.info(String.format("병렬 처리  평균: %6dms (최소: %3dms, 최대: %3dms)", parallelAvg, parallelMin, parallelMax));
		log.info(String.format("순차 처리  평균: %6dms (최소: %3dms, 최대: %3dms)", sequentialAvg, sequentialMin, sequentialMax));
		log.info("----------------------------------------");
		log.info(String.format("성능 개선:  %.2f배 빠름", improvementRatio));
		log.info(String.format("시간 절감:  %dms (평균)", timeSaved));
		log.info("----------------------------------------");
		log.info("기대값 계산:");
		log.info(String.format("  - 순차: 친구 수(5) × API 시간(100ms) = ~500ms"));
		log.info(String.format("  - 병렬: API 시간(100ms) + 스레드풀 오버헤드 = ~150-200ms"));
		log.info("========================================\n");

		// 5. 데이터 검증
		var parallelResult = coordinateAddressService.extractAndConvertCoordinatesWithCoordinates(friends);
		var sequentialResult = coordinateAddressService.extractAndConvertCoordinatesSequential(friends);

		assertNotNull(parallelResult.coordinates(), "병렬 처리 좌표 결과가 null이 아니어야 함");
		assertNotNull(parallelResult.addresses(), "병렬 처리 주소 결과가 null이 아니어야 함");
		assertNotNull(sequentialResult.coordinates(), "순차 처리 좌표 결과가 null이 아니어야 함");
		assertNotNull(sequentialResult.addresses(), "순차 처리 주소 결과가 null이 아니어야 함");
		assertEquals(friends.size(), parallelResult.addresses().size(), "병렬 처리 주소 개수 검증");
		assertEquals(friends.size(), sequentialResult.addresses().size(), "순차 처리 주소 개수 검증");

		log.info("✓ 데이터 검증 완료 - 양쪽 모두 {}개의 주소 조회\n", parallelResult.addresses().size());
	}

	@Test
	@DisplayName("대규모 성능 비교 - 친구 20명 (스레드풀 테스트)")
	void largeScalePerformanceComparison_20Friends() {
		// 테스트 데이터 준비 (친구 20명)
		List<FriendRunningStatusProjection> friends = createMockFriends(20);

		log.info("\n========================================");
		log.info("대규모 성능 비교 테스트: 친구 20명");
		log.info("친구 수: {}", friends.size());
		log.info("API 응답 시간: 100ms (mock)");
		log.info("스레드풀 최대 동시: 5 (maxConcurrentRequests)");
		log.info("========================================\n");

		// 1. 병렬 처리 성능 측정
		log.info("[1] 병렬 처리 실행 중...");
		long parallelStart = System.nanoTime();
		var parallelResult = coordinateAddressService
			.extractAndConvertCoordinatesWithCoordinates(friends);
		long parallelEnd = System.nanoTime();
		long parallelTimeMs = (parallelEnd - parallelStart) / 1_000_000;

		log.info("✓ 병렬 처리 완료: {}ms", parallelTimeMs);

		// 2. 순차 처리 성능 측정
		log.info("[2] 순차 처리 실행 중 (이 작업은 시간이 걸립니다)...");
		long sequentialStart = System.nanoTime();
		var sequentialResult = coordinateAddressService
			.extractAndConvertCoordinatesSequential(friends);
		long sequentialEnd = System.nanoTime();
		long sequentialTimeMs = (sequentialEnd - sequentialStart) / 1_000_000;

		log.info("✓ 순차 처리 완료: {}ms", sequentialTimeMs);

		// 3. 결과 비교
		double improvementRatio = sequentialTimeMs / (double)parallelTimeMs;

		log.info("\n========================================");
		log.info("대규모 성능 비교 결과");
		log.info("========================================");
		log.info(String.format("병렬 처리:  %6dms", parallelTimeMs));
		log.info(String.format("순차 처리:  %6dms", sequentialTimeMs));
		log.info("----------------------------------------");
		log.info(String.format("성능 개선:  %.2f배 빠름", improvementRatio));
		log.info(String.format("시간 절감:  %dms", sequentialTimeMs - parallelTimeMs));
		log.info("----------------------------------------");
		log.info("기대값 계산:");
		log.info(String.format("  - 순차: 친구 수(20) × API 시간(100ms) = ~2000ms"));
		log.info(String.format("  - 병렬: (친구 수(20) / 스레드풀(5)) × API 시간(100ms) + 오버헤드 = ~400-500ms"));
		log.info("========================================\n");

		assertNotNull(parallelResult.addresses(), "병렬 처리 결과가 null이 아니어야 함");
		assertNotNull(sequentialResult.addresses(), "순차 처리 결과가 null이 아니어야 함");
		assertEquals(friends.size(), parallelResult.addresses().size(), "병렬 처리 주소 개수 검증");
		assertEquals(friends.size(), sequentialResult.addresses().size(), "순차 처리 주소 개수 검증");
	}

	@Test
	@DisplayName("초대규모 성능 비교 - 친구 50명 (최악의 시나리오)")
	void largeScalePerformanceComparison_50Friends() {
		// 테스트 데이터 준비 (친구 50명)
		List<FriendRunningStatusProjection> friends = createMockFriends(50);

		log.info("\n========================================");
		log.info("초대규모 성능 비교 테스트: 친구 50명");
		log.info("친구 수: {}", friends.size());
		log.info("API 응답 시간: 100ms (mock)");
		log.info("스레드풀 최대 동시: 5 (maxConcurrentRequests)");
		log.info("========================================\n");

		// 1. 병렬 처리 성능 측정
		log.info("[1] 병렬 처리 실행 중...");
		long parallelStart = System.nanoTime();
		var parallelResult = coordinateAddressService
			.extractAndConvertCoordinatesWithCoordinates(friends);
		long parallelEnd = System.nanoTime();
		long parallelTimeMs = (parallelEnd - parallelStart) / 1_000_000;

		log.info("✓ 병렬 처리 완료: {}ms", parallelTimeMs);

		// 순차 처리는 너무 오래 걸리므로 스킵하고, 예상 시간만 표시
		long estimatedSequentialMs = (long) (friends.size() * 100);

		log.info("\n========================================");
		log.info("성능 비교 결과");
		log.info("========================================");
		log.info(String.format("병렬 처리:  %6dms (실제)", parallelTimeMs));
		log.info(String.format("순차 처리:  ~%6dms (예상)", estimatedSequentialMs));
		log.info("----------------------------------------");
		double estimatedRatio = estimatedSequentialMs / (double)parallelTimeMs;
		log.info(String.format("성능 개선:  ~%.2f배 빠름 (예상)", estimatedRatio));
		log.info(String.format("시간 절감:  ~%dms (예상)", estimatedSequentialMs - parallelTimeMs));
		log.info("----------------------------------------");
		log.info("기대값 계산:");
		log.info(String.format("  - 순차: 친구 수(50) × API 시간(100ms) = ~5000ms"));
		log.info(String.format("  - 병렬: (친구 수(50) / 스레드풀(5)) × API 시간(100ms) + 오버헤드 = ~1000-1100ms"));
		log.info("========================================\n");

		assertNotNull(parallelResult.addresses(), "병렬 처리 결과가 null이 아니어야 함");
		assertEquals(friends.size(), parallelResult.addresses().size(), "병렬 처리 주소 개수 검증");
	}

	// ==========================================
	// 테스트 데이터 생성
	// ==========================================

	private List<FriendRunningStatusProjection> createMockFriends(int count) {
		List<FriendRunningStatusProjection> friends = new ArrayList<>();

		for (long i = 1; i <= count; i++) {
			FriendRunningStatusProjection mockFriend = mock(FriendRunningStatusProjection.class);

			// 기본 정보 설정
			when(mockFriend.getUserId()).thenReturn(i);
			when(mockFriend.getNickname()).thenReturn("Friend_" + i);
			when(mockFriend.getProfileImage()).thenReturn("https://example.com/profile/" + i);
			when(mockFriend.getLatestRanAt()).thenReturn(java.time.LocalDateTime.now());
			when(mockFriend.getIsMe()).thenReturn(0);

			// 러닝 세그먼트 데이터 설정
			RunSegmentData segment = mock(RunSegmentData.class);
			when(segment.latitude()).thenReturn(37.4979 + (i * 0.01));  // 위도
			when(segment.longitude()).thenReturn(127.0276 + (i * 0.01)); // 경도
			when(segment.distance()).thenReturn(i * 100L);
			when(segment.time()).thenReturn(java.time.LocalDateTime.now());

			// RunSegmentInfo 설정
			RunSegmentInfo runSegmentInfo = mock(RunSegmentInfo.class);
			when(runSegmentInfo.segments()).thenReturn(List.of(segment));

			when(mockFriend.getRunSegmentData()).thenReturn(runSegmentInfo);

			friends.add(mockFriend);
		}

		return friends;
	}
}
