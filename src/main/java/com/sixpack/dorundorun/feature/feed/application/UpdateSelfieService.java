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
		String newSelfieImageKey = uploadNewImageIfNeeded(selfieImage);

		Feed feed = feedJpaRepository.findById(feedId)
			.filter(f -> f.getDeletedAt() == null)
			.orElseThrow(() -> FeedErrorCode.NOT_FOUND_FEED.format(feedId));

		if (!feed.getUser().getId().equals(user.getId())) {
			throw FeedErrorCode.FORBIDDEN_FEED_ACCESS.format();
		}

		String oldSelfieImageKey = feed.getSelfieImageKey();

		String finalImageKey = determineFinalImageKey(oldSelfieImageKey, request.deleteSelfieImage(),
			newSelfieImageKey);

		feed.update(request.content(), finalImageKey);

		deleteOldImageIfNeeded(oldSelfieImageKey, finalImageKey);

		return feed;
	}

	private String uploadNewImageIfNeeded(MultipartFile newSelfieImage) {
		if (newSelfieImage != null && !newSelfieImage.isEmpty()) {
			String uploadedKey = s3Service.uploadImage(newSelfieImage);
			return uploadedKey;
		}
		return null;
	}

	private String determineFinalImageKey(String oldImageKey, Boolean deleteSelfieImage, String uploadedImageKey) {
		if (uploadedImageKey != null) {
			return uploadedImageKey;
		}

		if (Boolean.TRUE.equals(deleteSelfieImage)) {
			return null;
		}

		return oldImageKey;
	}

	private void deleteOldImageIfNeeded(String oldImageKey, String newImageKey) {
		if (oldImageKey != null && !oldImageKey.equals(newImageKey)) {
			s3Service.deleteImage(oldImageKey);
		}
	}
}