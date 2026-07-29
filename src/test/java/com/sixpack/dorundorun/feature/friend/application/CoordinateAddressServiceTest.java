package com.sixpack.dorundorun.feature.friend.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.sixpack.dorundorun.feature.friend.dao.projection.FriendRunningStatusProjection;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentInfo;
import com.sixpack.dorundorun.global.config.naver.ReverseGeocodingProperties;
import com.sixpack.dorundorun.infra.naver.dto.AddressInfo;

@DisplayName("CoordinateAddressService 테스트")
class CoordinateAddressServiceTest {

	@Mock
	private ReverseGeocodingService reverseGeocodingService;

	private CoordinateAddressService coordinateAddressService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);

		ReverseGeocodingProperties.Api apiProperties = new ReverseGeocodingProperties.Api(5, 3, 1000, "주소 미상");
		ReverseGeocodingProperties properties = new ReverseGeocodingProperties(null, apiProperties);

		coordinateAddressService = new CoordinateAddressService(reverseGeocodingService, properties);

		when(reverseGeocodingService.addressByCoordinatesAsync(anyDouble(), anyDouble()))
			.thenAnswer(invocation -> {
				Double lat = invocation.getArgument(0);
				Double lon = invocation.getArgument(1);
				return CompletableFuture.completedFuture(
					new AddressInfo("Test Address (" + lat + "," + lon + ")", LocalDateTime.now()));
			});
	}

	@Test
	@DisplayName("친구 목록의 좌표를 모두 주소로 변환한다")
	void extractAndConvertCoordinates_convertsAllFriends() {
		List<FriendRunningStatusProjection> friends = createMockFriends(5);

		var result = coordinateAddressService.extractAndConvertCoordinatesWithCoordinates(friends);

		assertEquals(friends.size(), result.addresses().size(), "주소 개수 검증");
		assertEquals(friends.size(), result.coordinates().size(), "좌표 개수 검증");
	}

	@Test
	@DisplayName("부분 실패 테스트 - 일부 API 호출 실패 시 전체 실패하지 않음")
	void partialFailureTest() {
		List<FriendRunningStatusProjection> friends = createMockFriends(5);

		// 특정 좌표에서 실패하도록 설정
		when(reverseGeocodingService.addressByCoordinatesAsync(eq(37.4979 + 0.03), eq(127.0276 + 0.03)))
			.thenReturn(CompletableFuture.failedFuture(
				new RuntimeException("API 호출 실패 시뮬레이션")));

		var result = coordinateAddressService.extractAndConvertCoordinatesWithCoordinates(friends);

		assertEquals(friends.size(), result.addresses().size(), "실패 항목 포함 전체 결과 반환");

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
