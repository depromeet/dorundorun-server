package com.sixpack.dorundorun.feature.friend.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.global.config.naver.ReverseGeocodingProperties;
import com.sixpack.dorundorun.infra.naver.api.NaverReverseGeocodingApi;
import com.sixpack.dorundorun.infra.naver.dto.response.ReverseGeocodingResponse;

@DisplayName("ReverseGeocodingService 캐시 스탬피드 재현 테스트")
class ReverseGeocodingServiceStampedeTest {

	private static final double LATITUDE = 37.4979;
	private static final double LONGITUDE = 127.0276;
	private static final int CONCURRENT_REQUESTS = 20;

	@Mock
	private StringRedisTemplate redisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private NaverReverseGeocodingApi naverReverseGeocodingApi;

	private ReverseGeocodingService reverseGeocodingService;
	private ExecutorService executor;
	private AtomicInteger apiCallCount;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);
		apiCallCount = new AtomicInteger(0);

		// 캐시가 항상 비어있는 상황(콜드 캐시 / 동시 미스)을 시뮬레이션
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(anyString())).thenReturn(null);

		// API는 호출될 때마다 지연을 주어 동시 호출 구간을 넓힘
		when(naverReverseGeocodingApi.reverseGeocode(anyDouble(), anyDouble()))
			.thenAnswer(invocation -> {
				apiCallCount.incrementAndGet();
				Thread.sleep(100);
				return fakeResponse();
			});

		ReverseGeocodingProperties.Cache cacheProperties =
			new ReverseGeocodingProperties.Cache(1, true, 3);
		ReverseGeocodingProperties.Api apiProperties =
			new ReverseGeocodingProperties.Api(CONCURRENT_REQUESTS, 0, 0, "주소 미상");
		ReverseGeocodingProperties properties =
			new ReverseGeocodingProperties(cacheProperties, apiProperties);

		reverseGeocodingService = new ReverseGeocodingService(
			redisTemplate, naverReverseGeocodingApi, new ObjectMapper(), properties);
	}

	@AfterEach
	void tearDown() {
		executor.shutdownNow();
	}

	@Test
	@DisplayName("같은 좌표에 대한 동시 요청은 외부 API를 단 한 번만 호출해야 한다")
	void concurrentRequestsForSameCoordinate_shouldCallApiOnlyOnce() {
		CompletableFuture<?>[] futures = new CompletableFuture[CONCURRENT_REQUESTS];
		for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
			futures[i] = CompletableFuture.runAsync(
				() -> reverseGeocodingService.addressByCoordinatesAsync(LATITUDE, LONGITUDE).join(), executor);
		}

		CompletableFuture.allOf(futures).join();

		assertEquals(1, apiCallCount.get(),
			"동시 요청 " + CONCURRENT_REQUESTS + "건 중 API 호출은 1회여야 하지만 " + apiCallCount.get() + "회 발생함 (캐시 스탬피드)");
	}

	private ReverseGeocodingResponse fakeResponse() {
		ReverseGeocodingResponse.Area area1 = new ReverseGeocodingResponse.Area("서울특별시");
		ReverseGeocodingResponse.Area area2 = new ReverseGeocodingResponse.Area("마포구");
		ReverseGeocodingResponse.Region region = new ReverseGeocodingResponse.Region(area1, area2, null);
		ReverseGeocodingResponse.Result result = new ReverseGeocodingResponse.Result(region);
		return new ReverseGeocodingResponse(null, List.of(result));
	}
}
