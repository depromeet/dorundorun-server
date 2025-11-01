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

	public void delete(Long feedId, User user) {
		// 삭제할 이미지 키들을 먼저 조회
		String selfieImageKey = deleteFeedInTransaction(feedId, user);

		// 트랜잭션 커밋 후 S3에서 이미지 삭제
		if (selfieImageKey != null) {
			s3Service.deleteImage(selfieImageKey);
		}
	}

	@Transactional
	public String deleteFeedInTransaction(Long feedId, User user) {
		Feed feed = feedJpaRepository.findById(feedId)
			.filter(f -> f.getDeletedAt() == null)
			.orElseThrow(() -> FeedErrorCode.NOT_FOUND_FEED.format(feedId));

		// 본인의 피드인지 확인
		if (!feed.getUser().getId().equals(user.getId())) {
			throw FeedErrorCode.FORBIDDEN_FEED_ACCESS.format();
		}

		// 삭제 전에 이미지 키 저장
		String selfieImageKey = feed.getSelfieImageKey();

		// Hard delete (트랜잭션 내)
		feedJpaRepository.delete(feed);

		// 이미지 키 반환 (트랜잭션 커밋 후 S3 삭제용)
		return selfieImageKey;
	}
}
