package com.sixpack.dorundorun.feature.feed.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse;
import com.sixpack.dorundorun.feature.friend.dao.FriendJpaRepository;
import com.sixpack.dorundorun.feature.run.dao.RunSessionJpaRepository;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.application.GetDefaultProfileImageUrlService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FindUserSummaryService {

	private final FindUserByIdService findUserByIdService;
	private final FriendJpaRepository friendJpaRepository;
	private final RunSessionJpaRepository runSessionJpaRepository;
	private final FeedJpaRepository feedJpaRepository;
	private final S3Service s3Service;
	private final GetDefaultProfileImageUrlService getDefaultProfileImageUrlService;

	@Transactional(readOnly = true)
	public SelfieFeedResponse.UserSummary find(Long userId) {
		// 1. 유저 조회
		User user = findUserByIdService.find(userId);

		// 2. 친구 수 조회
		long friendCount = friendJpaRepository.countByUserIdAndDeletedAtIsNull(userId);

		// 3. 누적 거리 조회 (미터 그대로)
		Long totalDistanceMeters = runSessionJpaRepository.sumDistanceByUserId(userId);

		// 4. 인증 횟수 조회
		long selfieCount = feedJpaRepository.countByUserIdAndDeletedAtIsNull(userId);

		// 5. S3 key를 Presigned URL로 변환
		String profileImageUrl = user.getProfileImageUrl() != null
			? s3Service.getImageUrl(user.getProfileImageUrl())
			: getDefaultProfileImageUrlService.get();

		return new SelfieFeedResponse.UserSummary(
			user.getNickname(),
			profileImageUrl,
			(int)friendCount,
			totalDistanceMeters,
			(int)selfieCount
		);
	}
}
