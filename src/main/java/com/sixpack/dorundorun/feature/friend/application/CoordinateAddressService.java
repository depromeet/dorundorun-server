package com.sixpack.dorundorun.feature.friend.application;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.friend.dao.projection.FriendRunningStatusProjection;
import com.sixpack.dorundorun.feature.friend.exception.ReverseGeocodingErrorCode;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;
import com.sixpack.dorundorun.global.config.webclient.naver.ReverseGeocodingProperties;
import com.sixpack.dorundorun.global.exception.CustomException;
import com.sixpack.dorundorun.infra.naver.dto.AddressInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CoordinateAddressService {

	private final ReverseGeocodingService reverseGeocodingService;
	private final ReverseGeocodingProperties reverseGeocodingProperties;

	public record CoordinateData(
		Double latitude,
		Double longitude,
		Long distance
	) {
	}

	public record CoordinateAndAddressData(
		Map<Long, CoordinateData> coordinates,
		Map<Long, AddressInfo> addresses
	) {
	}

	// 좌표를 추출하여 주소로 변환
	public CoordinateAndAddressData extractAndConvertCoordinatesWithCoordinates(
		List<FriendRunningStatusProjection> friends) {

		Map<Long, CoordinateData> coordinateMap = extractCoordinates(friends);

		Map<Long, AddressInfo> addressMap = convertCoordinates(coordinateMap);

		return new CoordinateAndAddressData(coordinateMap, addressMap);
	}

	// 좌표 정보를 추출하여 맵으로 반환
	private Map<Long, CoordinateData> extractCoordinates(List<FriendRunningStatusProjection> friends) {
		return friends.stream()
			.collect(Collectors.toMap(
				FriendRunningStatusProjection::getUserId,
				this::extractSegmentCoordinates
			));
	}

	// 친구의 최신 러닝 세그먼트 좌표 추출
	private CoordinateData extractSegmentCoordinates(FriendRunningStatusProjection friend) {

		var runSegmentInfo = friend.getRunSegmentData();

		if (runSegmentInfo == null || runSegmentInfo.segments().isEmpty()) {
			return new CoordinateData(null, null, null);
		}

		// 가장 최신 시간의 세그먼트 찾기
		RunSegmentData latestSegment = runSegmentInfo.segments().stream()
			.filter(segment -> segment.time() != null)
			.max(Comparator.comparing(RunSegmentData::time))
			.orElse(runSegmentInfo.segments().get(runSegmentInfo.segments().size() - 1));

		return new CoordinateData(
			latestSegment.latitude(),
			latestSegment.longitude(),
			latestSegment.distance()
		);
	}

	private Map<Long, AddressInfo> convertCoordinates(Map<Long, CoordinateData> coordinateMap) {
		// 스레드 풀 생성
		int maxConcurrency = (int)reverseGeocodingProperties.api().maxConcurrentRequests();
		ExecutorService executor = Executors.newFixedThreadPool(maxConcurrency);

		try {
			// 모든 작업을 CompletableFuture로 제출
			Map<Long, CompletableFuture<AddressInfo>> futures = new HashMap<>();

			for (Map.Entry<Long, CoordinateData> entry : coordinateMap.entrySet()) {
				// 모든 작업을 병렬로 제출 (블로킹 없음)
				CompletableFuture<AddressInfo> future = CompletableFuture.supplyAsync(
					() -> getAddress(entry.getValue()),
					executor
				);
				futures.put(entry.getKey(), future);
			}

			// 모든 작업 완료 대기 (진정한 병렬 처리)
			CompletableFuture.allOf(
				futures.values().toArray(new CompletableFuture[0])
			).join();

			// 결과 수집
			Map<Long, AddressInfo> addressMap = new HashMap<>();
			futures.forEach((key, future) -> {
				try {
					addressMap.put(key, future.join());
				} catch (CompletionException e) {
					// CompletionException에서 원본 예외 추출
					if (e.getCause() instanceof CustomException) {
						throw (CustomException)e.getCause();
					}
					throw ReverseGeocodingErrorCode.REVERSE_GEOCODING_FAILED.format(
						coordinateMap.get(key).latitude(), coordinateMap.get(key).longitude());
				}
			});

			return addressMap;
		} finally {
			executor.shutdown();
		}
	}

	private AddressInfo getAddress(CoordinateData coordinates) {
		// 좌표 유효성 확인
		if (coordinates.latitude() == null || coordinates.longitude() == null) {
			// 좌표가 없으면 기본 주소 반환
			return fallbackAddressInfo();
		}

		try {
			// 좌표로부터 주소 조회 (동기 처리)
			return reverseGeocodingService
				.addressByCoordinates(coordinates.latitude(), coordinates.longitude());
		} catch (CustomException e) {
			// 커스텀 예외는 그대로 재발생
			throw e;
		} catch (Exception e) {
			// 기타 예외는 처리됨 예외로 변환
			throw ReverseGeocodingErrorCode.REVERSE_GEOCODING_FAILED.format(
				coordinates.latitude(), coordinates.longitude());
		}
	}

	private AddressInfo fallbackAddressInfo() {
		return new AddressInfo(
			reverseGeocodingProperties.api().fallbackAddress(),
			LocalDateTime.now()
		);
	}

	// ============================================
	// TEST 용도: 순차 처리 (성능 비교용)
	// ============================================

	// 순차 처리로 좌표를 주소로 변환 (성능 비교용 테스트 메서드)
	public CoordinateAndAddressData extractAndConvertCoordinatesSequential(
		List<FriendRunningStatusProjection> friends) {

		Map<Long, CoordinateData> coordinateMap = extractCoordinates(friends);

		Map<Long, AddressInfo> addressMap = convertCoordinatesSequential(coordinateMap);

		return new CoordinateAndAddressData(coordinateMap, addressMap);
	}

	// 순차 처리: 각 좌표를 순차적으로 주소로 변환 (블로킹 발생)
	private Map<Long, AddressInfo> convertCoordinatesSequential(Map<Long, CoordinateData> coordinateMap) {
		// 순차 처리 (스레드 풀 미사용)
		Map<Long, AddressInfo> addressMap = new HashMap<>();

		for (Map.Entry<Long, CoordinateData> entry : coordinateMap.entrySet()) {
			try {
				// 각 좌표를 순차적으로 처리 (대기 발생)
				AddressInfo addressInfo = getAddress(entry.getValue());
				addressMap.put(entry.getKey(), addressInfo);
			} catch (CustomException e) {
				throw e;
			} catch (Exception e) {
				throw ReverseGeocodingErrorCode.REVERSE_GEOCODING_FAILED.format(
					entry.getValue().latitude(), entry.getValue().longitude());
			}
		}

		return addressMap;
	}
}
