package com.sixpack.dorundorun.feature.friend.application;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.friend.dao.projection.FriendRunningStatusProjection;
import com.sixpack.dorundorun.feature.friend.dto.response.FriendRunningStatusResponse;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentInfo;
import com.sixpack.dorundorun.feature.user.application.GetDefaultProfileImageUrlService;
import com.sixpack.dorundorun.global.response.PaginationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendsRunningStatusService {

	private final FindFriendsRunningStatusService findFriendsRunningStatusService;
	private final GetDefaultProfileImageUrlService getDefaultProfileImageUrlService;

	@Transactional(readOnly = true)
	public PaginationResponse<FriendRunningStatusResponse> find(Long userId, Integer page, Integer size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<FriendRunningStatusProjection> resultsPage = findFriendsRunningStatusService.find(userId, pageable);
		List<FriendRunningStatusResponse> responses = resultsPage.getContent()
			.stream()
			.map(this::mapToResponse)
			.toList();
		return PaginationResponse.of(responses, page, size, resultsPage.getTotalElements());
	}

	private FriendRunningStatusResponse mapToResponse(FriendRunningStatusProjection projection) {
		String profileImageUrl = projection.getProfileImage() != null
			? projection.getProfileImage()
			: getDefaultProfileImageUrlService.get();
		boolean isMe = projection.getIsMe() != null && projection.getIsMe() != 0;
		Long distance = null;
		Double latitude = null;
		Double longitude = null;
		if (projection.getRunSegmentData() != null) {
			RunSegmentInfo segmentInfo = projection.getRunSegmentData();
			if (segmentInfo.segments() != null && !segmentInfo.segments().isEmpty()) {
				List<RunSegmentData> segments = segmentInfo.segments();
				RunSegmentData latestSegment = segments.stream()
					.filter(segment -> segment.time() != null) // time이 null이 아닌 것
					.max(Comparator.comparing(RunSegmentData::time)) // time이 가장 큰 것
					.orElse(segments.get(segments.size() - 1)); // 없으면 마지막 세그먼트
				// 선택한 세그먼트에서 distance, latitude, longitude 추출
				distance = latestSegment.distance();
				latitude = latestSegment.latitude();
				longitude = latestSegment.longitude();
			}
		}
		return new FriendRunningStatusResponse(
			projection.getUserId(),
			isMe,
			projection.getNickname(),
			profileImageUrl,
			projection.getLatestRanAt(),
			distance,
			latitude,
			longitude
		);
	}

}