package com.sixpack.dorundorun.feature.friend.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.sixpack.dorundorun.feature.friend.dao.projection.FriendRunningStatusProjection;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentInfo;
import com.sixpack.dorundorun.global.config.naver.ReverseGeocodingProperties;
import com.sixpack.dorundorun.infra.naver.dto.AddressInfo;

@DisplayName("CoordinateAddressService 성능 비교 테스트")
class CoordinateAddressServiceTest {

	private static final Logger log = LoggerFactory.getLogger(CoordinateAddressServiceTest.class);

	@Mock
	private ReverseGeocodingService reverseGeocodingService;

	private CoordinateAddressService coordinateAddressService;
	private Executor testExecutor;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		// 테스트용 Executor
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(5);
		executor.setMaxPoolSize(5);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("test-geocoding-");
		executor.initialize();
		testExecutor = executor;

		ReverseGeocodingProperties.Api apiProperties = new ReverseGeocodingProperties.Api(5, 3, 1000, "주소 미상");
		ReverseGeocodingProperties properties = new ReverseGeocodingProperties(null, apiProperties);

		coordinateAddressService = new CoordinateAddressService(reverseGeocodingService, properties, testExecutor);

		// 비동기 메서드 모킹: 100ms 지연 시뮬레이션
		when(reverseGeocodingService.addressByCoordinatesAsync(anyDouble(), anyDouble(), any(Executor.class)))
			.thenAnswer(invocation -> {
				Double lat = invocation.getArgument(0);
				Double lon = invocation.getArgument(1);
				Executor exec = invocation.getArgument(2);
				return CompletableFuture.supplyAsync(() -> {
					try {
						Thread.sleep(100);  // 100ms API 지연
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					return new AddressInfo("Test Address (" + lat + "," + lon + ")", LocalDateTime.now());
				}, exec);
			});
	}

	@Test
	@DisplayName("병렬 처리 성능 테스트 - 친구 5명 (API 지연 100ms)")
	void performanceTest_5Friends() {
		List<FriendRunningStatusProjection> friends = createMockFriends(5);

		log.info("\n========================================");
		log.info("성능 테스트: 친구 5명");
		log.info("API 응답 시간: 100ms (mock)");
		log.info("스레드풀 크기: 5");
		log.info("========================================\n");

		// 워밍업
		coordinateAddressService.extractAndConvertCoordinatesWithCoordinates(friends);

		// 5회 반복 측정
		long[] times = new long[5];
		for (int i = 0; i < 5; i++) {
			long start = System.nanoTime();
			var result = coordinateAddressService.extractAndConvertCoordinatesWithCoordinates(friends);
			long end = System.nanoTime();
			times[i] = (end - start) / 1_000_000;
			log.info("   반복 {}: {}ms", i + 1, times[i]);
		}
		long avg = (long)java.util.Arrays.stream(times).average().orElse(0);
		long min = java.util.Arrays.stream(times).min().orElse(0);
		long max = java.util.Arrays.stream(times).max().orElse(0);

		log.info("\n========================================");
		log.info("결과: 평균 {}ms (최소: {}ms, 최대: {}ms)", avg, min, max);
		log.info("기대값: ~100ms + 오버헤드 (5개 병렬 → 1 batch)");
		log.info("순차 기대값: ~500ms");
		log.info("========================================\n");

		var result = coordinateAddressService.extractAndConvertCoordinatesWithCoordinates(friends);
		assertEquals(friends.size(), result.addresses().size(), "주소 개수 검증");
	}

	@Test
	@DisplayName("대규모 성능 테스트 - 친구 20명 (스레드풀 제한 확인)")
	void performanceTest_20Friends() {
		List<FriendRunningStatusProjection> friends = createMockFriends(20);

		log.info("\n========================================");
		log.info("대규모 성능 테스트: 친구 20명");
		log.info("스레드풀 크기: 5 → 4 batch 예상");
		log.info("========================================\n");

		long start = System.nanoTime();
		var result = coordinateAddressService.extractAndConvertCoordinatesWithCoordinates(friends);
		long elapsed = (System.nanoTime() - start) / 1_000_000;

		log.info("소요 시간: {}ms", elapsed);
		log.info("기대값: ~400ms (20/5 × 100ms + 오버헤드)");
		log.info("순차 기대값: ~2000ms\n");

		assertEquals(friends.size(), result.addresses().size(), "주소 개수 검증");
	}

	@Test
	@DisplayName("부분 실패 테스트 - 일부 API 호출 실패 시 전체 실패하지 않음")
	void partialFailureTest() {
		List<FriendRunningStatusProjection> friends = createMockFriends(5);

		// 특정 좌표에서 실패하도록 설정
		when(reverseGeocodingService.addressByCoordinatesAsync(
			eq(37.4979 + 0.03), eq(127.0276 + 0.03), any(Executor.class)))
			.thenReturn(CompletableFuture.failedFuture(
				new RuntimeException("API 호출 실패 시뮬레이션")));

		var result = coordinateAddressService.extractAndConvertCoordinatesWithCoordinates(friends);

		assertEquals(friends.size(), result.addresses().size(), "실패 항목 포함 전체 결과 반환");
		log.info("부분 실패 처리 확인: {}개 중 실패 항목은 '주소 미상'으로 대체",
			result.addresses().size());

		// 실패한 항목이 fallback 주소인지 확인
		long fallbackCount = result.addresses().values().stream()
			.filter(addr -> "주소 미상".equals(addr.address()))
			.count();
		assertTrue(fallbackCount >= 1, "최소 1개의 fallback 주소가 있어야 함");
	}

	private List<FriendRunningStatusProjection> createMockFriends(int count) {
		List<FriendRunningStatusProjection> friends = new ArrayList<>();

		for (long i = 1; i <= count; i++) {
			FriendRunningStatusProjection mockFriend = mock(FriendRunningStatusProjection.class);

			when(mockFriend.getUserId()).thenReturn(i);
			when(mockFriend.getNickname()).thenReturn("Friend_" + i);
			when(mockFriend.getProfileImage()).thenReturn("https://example.com/profile/" + i);
			when(mockFriend.getLatestRanAt()).thenReturn(LocalDateTime.now());
			when(mockFriend.getIsMe()).thenReturn(0);

			RunSegmentData segment = mock(RunSegmentData.class);
			when(segment.latitude()).thenReturn(37.4979 + (i * 0.01));
			when(segment.longitude()).thenReturn(127.0276 + (i * 0.01));
			when(segment.distance()).thenReturn(i * 100L);
			when(segment.time()).thenReturn(LocalDateTime.now());

			RunSegmentInfo runSegmentInfo = mock(RunSegmentInfo.class);
			when(runSegmentInfo.segments()).thenReturn(List.of(segment));

			when(mockFriend.getRunSegmentData()).thenReturn(runSegmentInfo);

			friends.add(mockFriend);
		}

		return friends;
	}
}
