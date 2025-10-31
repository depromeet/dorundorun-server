package com.sixpack.dorundorun.feature.feed.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.dto.request.CreateSelfieRequest;
import com.sixpack.dorundorun.feature.feed.exception.FeedErrorCode;
import com.sixpack.dorundorun.feature.run.application.FindRunSessionByIdAndUserIdService;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateSelfieService {

	private final FindRunSessionByIdAndUserIdService findRunSessionByIdAndUserIdService;
	private final FeedJpaRepository feedJpaRepository;
	private final S3Service s3Service;

	@Transactional
	public Feed create(User user, CreateSelfieRequest request, MultipartFile selfieImage) {
		RunSession runSession = findRunSessionByIdAndUserIdService.find(request.runSessionId(), user.getId());

		// 한 러닝 세션에 이미 셀피가 존재하는지 확인
		feedJpaRepository.findByRunSessionIdAndDeletedAtIsNull(request.runSessionId())
			.ifPresent(existingFeed -> {
				throw FeedErrorCode.ALREADY_EXISTS_FEED_FOR_RUN_SESSION.format(request.runSessionId());
			});

		Feed feed = Feed.builder()
			.user(user)
			.runSession(runSession)
			.mapImage(runSession.getMapImage())
			.selfieImage(getUploadedSelfieImage(selfieImage))
			.content(request.content())
			.build();

		return feedJpaRepository.save(feed);
	}

	private String getUploadedSelfieImage(MultipartFile selfieImage) {
		String uploadedSelfieImage = null;
		if (selfieImage != null && !selfieImage.isEmpty()) {
			uploadedSelfieImage = s3Service.uploadImage(selfieImage);
		}
		return uploadedSelfieImage;
	}
}
