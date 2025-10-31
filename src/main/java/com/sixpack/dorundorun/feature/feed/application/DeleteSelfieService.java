package com.sixpack.dorundorun.feature.feed.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.feed.dao.FeedJpaRepository;
import com.sixpack.dorundorun.feature.feed.domain.Feed;
import com.sixpack.dorundorun.feature.feed.exception.FeedErrorCode;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteSelfieService {

	private final FeedJpaRepository feedJpaRepository;
	private final S3Service s3Service;

	@Transactional
	public void delete(Long feedId, User user) {
		Feed feed = feedJpaRepository.findById(feedId)
			.filter(f -> f.getDeletedAt() == null)
			.orElseThrow(() -> FeedErrorCode.NOT_FOUND_FEED.format(feedId));

		// 본인의 피드인지 확인
		if (!feed.getUser().getId().equals(user.getId())) {
			throw FeedErrorCode.FORBIDDEN_FEED_ACCESS.format();
		}

		// S3에서 셀피 이미지 삭제
		String selfieImageKey = feed.getSelfieImageKey();
		if (selfieImageKey != null) {
			s3Service.deleteImage(selfieImageKey);
		}

		// Hard delete
		feedJpaRepository.delete(feed);
	}
}
