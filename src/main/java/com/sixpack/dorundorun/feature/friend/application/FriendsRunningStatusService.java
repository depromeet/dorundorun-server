package com.sixpack.dorundorun.feature.friend.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.friend.dao.projection.FriendRunningStatusProjection;
import com.sixpack.dorundorun.feature.friend.dto.response.FriendRunningStatusResponse;
import com.sixpack.dorundorun.feature.run.application.FindLatestRunSegmentByUserIdService;
import com.sixpack.dorundorun.feature.run.domain.RunSegment;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;
import com.sixpack.dorundorun.feature.run.domain.RunSegmentInfo;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.application.GetDefaultProfileImageUrlService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.response.PaginationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetFriendsRunningStatusService {

	private final FindUserByIdService findUserByIdService;
	private final FindFriendsRunningStatusService findFriendsRunningStatusService;
	private final FindLatestRunSegmentByUserIdService findLatestRunSegmentByUserIdService;
	private final GetDefaultProfileImageUrlService getDefaultProfileImageUrlService;

	@Transactional(readOnly = true)
	public PaginationResponse<FriendRunningStatusResponse> find(Long userId, Integer page, Integer size) {
		List<FriendRunningStatusResponse> responses = new ArrayList<>();

		// page=0일 때만 본인 정보 먼저 추가
		if (page == 0) {
			User user = findUserByIdService.find(userId);
			FriendRunningStatusResponse myStatus = getMyRunningStatus(user);
			responses.add(myStatus);
		}

		// 친구 데이터 조회
		Pageable pageable = PageRequest.of(page, size);
		Page<FriendRunningStatusProjection> friendsPage = findFriendsRunningStatusService.find(userId, pageable);

		// Projection → Response DTO 변환 및 추가
		friendsPage.getContent()
			.stream()
			.map(this::mapToResponse)
			.forEach(responses::add);

		// 전체 개수 = 친구 총원 + 본인 1명
		long totalElements = friendsPage.getTotalElements() + 1;

		return PaginationResponse.of(responses, page, size, totalElements);
	}

	private FriendRunningStatusResponse getMyRunningStatus(User user) {
		String defaultProfileImageUrl = getDefaultProfileImageUrlService.get();

		// 가장 최신 RunSegment 조회
		RunSegment latestSegment = findLatestRunSegmentByUserIdService.find(user.getId())
			.orElse(null);

		// RunSegment가 없는 경우
		if (latestSegment == null) {
			return new FriendRunningStatusResponse(
				user.getId(),
				true,
				user.getNickname(),
				defaultProfileImageUrl,
				null, null, null, null
			);
		}

		// RunSegmentInfo 추출 및 null 체크
		RunSegmentInfo data = latestSegment.getData();
		if (data == null || data.segments() == null || data.segments().isEmpty()) {
			return new FriendRunningStatusResponse(
				user.getId(),
				true,
				user.getNickname(),
				defaultProfileImageUrl,
				null, null, null, null
			);
		}

		// 마지막 세그먼트 데이터 추출
		List<RunSegmentData> segmentDataList = data.segments();
		RunSegmentData lastData = segmentDataList.get(segmentDataList.size() - 1);

		return new FriendRunningStatusResponse(
			user.getId(),
			true,
			user.getNickname(),
			defaultProfileImageUrl,
			latestSegment.getCreatedAt(),
			lastData.distance(),
			lastData.latitude(),
			lastData.longitude()
		);
	}

	// Projection을 Response DTO로 변환
	private FriendRunningStatusResponse mapToResponse(FriendRunningStatusProjection projection) {

		String profileImageUrl = projection.getProfileImage() != null
			? projection.getProfileImage()
			: getDefaultProfileImageUrlService.get();

		// isMe 플래그 변환 (Integer → Boolean)
		boolean isMe = projection.getIsMe() != null && projection.getIsMe() != 0;

		return new FriendRunningStatusResponse(
			projection.getUserId(),
			isMe,
			projection.getNickname(),
			profileImageUrl,
			projection.getLatestRanAt(),
			projection.getDistance(),
			projection.getLatitude(),
			projection.getLongitude()
		);
	}
}
