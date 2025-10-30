package com.sixpack.dorundorun.feature.friend.application;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.friend.dao.projection.FriendRunningStatusProjection;
import com.sixpack.dorundorun.feature.friend.dto.response.FriendRunningStatusResponse;
import com.sixpack.dorundorun.feature.friend.exception.ReverseGeocodingErrorCode;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;
import com.sixpack.dorundorun.feature.user.application.GetDefaultProfileImageUrlService;
import com.sixpack.dorundorun.global.config.webclient.naver.ReverseGeocodingProperties;
import com.sixpack.dorundorun.global.exception.CustomException;
import com.sixpack.dorundorun.global.response.PaginationResponse;
import com.sixpack.dorundorun.infra.naver.dto.AddressInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FriendsRunningStatusService {

	private record SegmentCoordinates(
		Double latitude,
		Double longitude,
		Long distance
	) {
	}

	private final FindFriendsRunningStatusService findFriendsRunningStatusService;
	private final GetDefaultProfileImageUrlService getDefaultProfileImageUrlService;
	private final ReverseGeocodingService reverseGeocodingService;
	private final ReverseGeocodingProperties reverseGeocodingProperties;

	@Transactional(readOnly = true)
	public PaginationResponse<FriendRunningStatusResponse> find(Long userId, Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<FriendRunningStatusProjection> resultsPage = findFriendsRunningStatusService.find(userId, pageable);

		List<FriendRunningStatusProjection> friends = resultsPage.getContent();

		if (friends.isEmpty()) {
			return PaginationResponse.of(List.of(), page, size, 0);
		}

		Map<Long, SegmentCoordinates> coordinateMap = friends.stream()
			.collect(Collectors.toMap(
				FriendRunningStatusProjection::getUserId,
				this::extractSegmentCoordinates
			));

		int maxConcurrency = (int)reverseGeocodingProperties.api().maxConcurrentRequests();
		ExecutorService executor = Executors.newFixedThreadPool(maxConcurrency);

		try {
			Map<Long, AddressInfo> addressMap = coordinateMap.entrySet().stream()
				.collect(Collectors.toMap(
					Map.Entry::getKey,
					entry -> {
						try {
							Future<AddressInfo> future = executor.submit(() ->
								getAddress(entry.getValue())
							);
							return future.get();
						} catch (CustomException e) {
							throw e;
						} catch (Exception e) {
							throw ReverseGeocodingErrorCode.REVERSE_GEOCODING_FAILED.format(
								entry.getValue().latitude(), entry.getValue().longitude());
						}
					}
				));

			List<FriendRunningStatusResponse> responses = friends.stream()
				.map(projection -> buildResponseWithAddress(
					projection,
					addressMap,
					coordinateMap.get(projection.getUserId())
				))
				.toList();

			return PaginationResponse.of(responses, page, size, resultsPage.getTotalElements());
		} finally {
			executor.shutdown();
		}
	}

	private AddressInfo getAddress(SegmentCoordinates coordinates) {
		if (coordinates.latitude() == null || coordinates.longitude() == null) {
			return fallbackAddressInfo();
		}

		try {
			return reverseGeocodingService
				.addressByCoordinates(coordinates.latitude(), coordinates.longitude())
				.block();
		} catch (CustomException e) {
			throw e;
		} catch (Exception e) {
			throw ReverseGeocodingErrorCode.REVERSE_GEOCODING_FAILED.format(
				coordinates.latitude(), coordinates.longitude());
		}
	}

	private SegmentCoordinates extractSegmentCoordinates(FriendRunningStatusProjection friend) {
		var runSegmentInfo = friend.getRunSegmentData();

		if (runSegmentInfo == null || runSegmentInfo.segments().isEmpty()) {
			return new SegmentCoordinates(null, null, null);
		}

		RunSegmentData latestSegment = runSegmentInfo.segments().stream()
			.filter(segment -> segment.time() != null)
			.max(Comparator.comparing(RunSegmentData::time))
			.orElse(runSegmentInfo.segments().get(runSegmentInfo.segments().size() - 1));

		return new SegmentCoordinates(
			latestSegment.latitude(),
			latestSegment.longitude(),
			latestSegment.distance()
		);
	}

	private AddressInfo fallbackAddressInfo() {
		return new AddressInfo(
			reverseGeocodingProperties.api().fallbackAddress(),
			LocalDateTime.now()
		);
	}

	private FriendRunningStatusResponse buildResponseWithAddress(
		FriendRunningStatusProjection projection,
		Map<Long, AddressInfo> addressMap,
		SegmentCoordinates coordinates) {

		String profileImageUrl = projection.getProfileImage() != null
			? projection.getProfileImage()
			: getDefaultProfileImageUrlService.get();

		boolean isMe = projection.getIsMe() != null && projection.getIsMe() != 0;

		// 주소맵에서 조회
		String address = addressMap
			.getOrDefault(projection.getUserId(),
				new AddressInfo(reverseGeocodingProperties.api().fallbackAddress(),
					LocalDateTime.now()))
			.address();

		Long distance = coordinates.distance();
		Double latitude = coordinates.latitude();
		Double longitude = coordinates.longitude();

		return new FriendRunningStatusResponse(
			projection.getUserId(),
			isMe,
			projection.getNickname(),
			profileImageUrl,
			projection.getLatestRanAt(),
			distance,
			latitude,
			longitude,
			address
		);
	}

}