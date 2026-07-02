package com.sixpack.dorundorun.feature.friend.application;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.friend.dao.projection.FriendRunningStatusProjection;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;
import com.sixpack.dorundorun.global.config.naver.ReverseGeocodingProperties;
import com.sixpack.dorundorun.infra.naver.dto.AddressInfo;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CoordinateAddressService {

	private final ReverseGeocodingService reverseGeocodingService;
	private final ReverseGeocodingProperties reverseGeocodingProperties;

	public CoordinateAddressService(
		ReverseGeocodingService reverseGeocodingService,
		ReverseGeocodingProperties reverseGeocodingProperties
	) {
		this.reverseGeocodingService = reverseGeocodingService;
		this.reverseGeocodingProperties = reverseGeocodingProperties;
	}

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

	public CoordinateAndAddressData extractAndConvertCoordinatesWithCoordinates(
		List<FriendRunningStatusProjection> friends) {

		Map<Long, CoordinateData> coordinateMap = extractCoordinates(friends);
		Map<Long, AddressInfo> addressMap = convertCoordinates(coordinateMap);

		return new CoordinateAndAddressData(coordinateMap, addressMap);
	}

	private Map<Long, CoordinateData> extractCoordinates(List<FriendRunningStatusProjection> friends) {
		return friends.stream()
			.collect(Collectors.toMap(
				FriendRunningStatusProjection::getUserId,
				this::extractSegmentCoordinates
			));
	}

	private CoordinateData extractSegmentCoordinates(FriendRunningStatusProjection friend) {
		var runSegmentInfo = friend.getRunSegmentData();

		if (runSegmentInfo == null || runSegmentInfo.segments().isEmpty()) {
			return new CoordinateData(null, null, null);
		}

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
		Map<Long, AddressInfo> addressMap = new HashMap<>();

		for (Map.Entry<Long, CoordinateData> entry : coordinateMap.entrySet()) {
			CoordinateData coord = entry.getValue();

			if (coord.latitude() == null || coord.longitude() == null) {
				addressMap.put(entry.getKey(), fallbackAddressInfo());
				continue;
			}

			AddressInfo addressInfo = reverseGeocodingService
				.addressByCoordinatesAsync(coord.latitude(), coord.longitude())
				.exceptionally(ex -> {
					log.warn("주소 변환 실패, 기본값 반환: lat={}, lon={}, error={}",
						coord.latitude(), coord.longitude(), ex.getMessage());
					return fallbackAddressInfo();
				})
				.join();

			addressMap.put(entry.getKey(), addressInfo);
		}

		return addressMap;
	}

	private AddressInfo fallbackAddressInfo() {
		return new AddressInfo(
			reverseGeocodingProperties.api().fallbackAddress(),
			LocalDateTime.now()
		);
	}
}
