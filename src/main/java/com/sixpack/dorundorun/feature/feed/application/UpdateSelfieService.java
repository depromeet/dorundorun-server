package com.sixpack.dorundorun.feature.feed.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.dto.request.UpdateSelfieRequest;
import com.sixpack.dorundorun.feature.feed.exception.FeedErrorCode;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateSelfieService {

	private final FeedJpaRepository feedJpaRepository;
	private final S3Service s3Service;

	@Transactional
	public Feed update(Long feedId, User user, UpdateSelfieRequest request, MultipartFile selfieImage) {
		Feed feed = feedJpaRepository.findById(feedId)
			.filter(f -> f.getDeletedAt() == null)
			.orElseThrow(() -> FeedErrorCode.NOT_FOUND_FEED.format(feedId));

		if (!feed.getUser().getId().equals(user.getId())) {
			throw FeedErrorCode.FORBIDDEN_FEED_ACCESS.format();
		}

		String oldSelfieImageKey = feed.getSelfieImageKey();
		String newSelfieImageKey = determineNewSelfieImageKey(oldSelfieImageKey, request.deleteSelfieImage(),
			selfieImage);

		// 이미지가 변경되는 경우 (새 이미지 업로드 또는 삭제) 기존 이미지를 S3에서 삭제
		if (oldSelfieImageKey != null && !oldSelfieImageKey.equals(newSelfieImageKey)) {
			s3Service.deleteImage(oldSelfieImageKey);
		}

		feed.update(request.content(), newSelfieImageKey);

		return feed;
	}

	private String determineNewSelfieImageKey(String oldImageKey, Boolean deleteSelfieImage,
		MultipartFile newSelfieImage) {
		// 1. 새 이미지가 업로드된 경우 -> 새 이미지로 교체
		if (newSelfieImage != null && !newSelfieImage.isEmpty()) {
			return s3Service.uploadImage(newSelfieImage);
		}

		// 2. 명시적으로 이미지 삭제를 요청한 경우 -> null 반환
		if (Boolean.TRUE.equals(deleteSelfieImage)) {
			return null;
		}

		// 3. 그 외의 경우 -> 기존 이미지 유지
		return oldImageKey;
	}
}