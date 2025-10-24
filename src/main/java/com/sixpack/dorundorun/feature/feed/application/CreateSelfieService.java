package com.sixpack.dorundorun.feature.feed.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.dto.request.CreateSelfieRequest;
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
		// 1. RunSession 조회 및 검증
		RunSession runSession = findRunSessionByIdAndUserIdService.find(
			request.runSessionId(),
			user.getId()
		);

		// 2. 셀피 이미지 S3 업로드 (선택사항)
		String uploadedSelfieImage = null;
		if (selfieImage != null && !selfieImage.isEmpty()) {
			uploadedSelfieImage = s3Service.uploadImage(selfieImage, "feed/selfie");
		}

		// 3. Feed 생성
		Feed feed = Feed.builder()
			.user(user)
			.runSession(runSession)
			.mapImage(runSession.getMapImage())
			.selfieImage(uploadedSelfieImage)
			.content(request.content())
			.build();

		// 4. Feed 저장
		return feedJpaRepository.save(feed);
	}
}
