package com.sixpack.dorundorun.feature.friend.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.friend.dao.projection.FriendRunningStatusProjection;
import com.sixpack.dorundorun.feature.friend.dto.response.FriendRunningStatusResponse;
import com.sixpack.dorundorun.feature.user.application.GetDefaultProfileImageUrlService;
import com.sixpack.dorundorun.global.config.webclient.naver.ReverseGeocodingProperties;
import com.sixpack.dorundorun.global.response.PaginationResponse;
import com.sixpack.dorundorun.infra.naver.dto.AddressInfo;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendsRunningStatusService {

	private final FindFriendsRunningStatusService findFriendsRunningStatusService;
	private final LatestCheerInfoService cheerInfoService;
	private final CoordinateAddressService coordinateAddressService;
	private final GetDefaultProfileImageUrlService getDefaultProfileImageUrlService;
	private final ReverseGeocodingProperties reverseGeocodingProperties;
	private final S3Service s3Service;

	@Transactional(readOnly = true)
	public PaginationResponse<FriendRunningStatusResponse> find(Long userId, Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<FriendRunningStatusProjection> resultsPage = findFriendsRunningStatusService.find(userId, pageable);

		List<FriendRunningStatusProjection> friends = resultsPage.getContent();

		if (friends.isEmpty()) {
			return PaginationResponse.of(List.of(), page, size, 0);
		}

		List<Long> friendIds = friends.stream()
			.filter(p -> p.getUserId() != null)
			.map(FriendRunningStatusProjection::getUserId)
			.toList();

		// 친구 최신 응원 기록 조회
		var cheerMap = cheerInfoService.getLatestCheerMap(userId, friendIds);
		// 친구의 좌표 추출 및 주소 변환
		var coordinateAndAddressData = coordinateAddressService.extractAndConvertCoordinatesWithCoordinates(friends);

		List<FriendRunningStatusResponse> responses = buildResponses(
			friends,
			cheerMap,
			coordinateAndAddressData.coordinates(),
			coordinateAndAddressData.addresses()
		);

		return PaginationResponse.of(responses, page, size, resultsPage.getTotalElements());
	}

	private List<FriendRunningStatusResponse> buildResponses(
		List<FriendRunningStatusProjection> friends,
		Map<Long, LocalDateTime> cheerMap,
		Map<Long, CoordinateAddressService.CoordinateData> coordinateMap,
		Map<Long, AddressInfo> addressMap) {

		return friends.stream()
			.map(projection -> buildResponse(
				projection,
				addressMap,
				coordinateMap.getOrDefault(projection.getUserId(),
					new CoordinateAddressService.CoordinateData(null, null, null)),
				cheerMap.get(projection.getUserId())
			))
			.toList();
	}

	private FriendRunningStatusResponse buildResponse(
		FriendRunningStatusProjection projection,
		Map<Long, AddressInfo> addressMap,
		CoordinateAddressService.CoordinateData coordinates,
		LocalDateTime latestCheeredAt) {

		String profileImageUrl =
			projection.getProfileImage() != null
				? s3Service.getImageUrl(projection.getProfileImage())
				: getDefaultProfileImageUrlService.get();

		boolean isMe = projection.getIsMe() != null && projection.getIsMe() != 0;

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
			latestCheeredAt,
			distance,
			latitude,
			longitude,
			address
		);
	}

}