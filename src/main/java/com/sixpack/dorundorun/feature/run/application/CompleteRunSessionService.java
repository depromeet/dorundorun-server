package com.sixpack.dorundorun.feature.run.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.feature.feed.event.FeedReminderRequestedEvent;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.run.dto.request.CompleteRunRequest;
import com.sixpack.dorundorun.feature.run.dto.response.RunSessionResponse;
import com.sixpack.dorundorun.feature.run.event.RunningProgressReminderRequestedEvent;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;
import com.sixpack.dorundorun.infra.s3.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompleteRunSessionService {

	private final FindRunSessionByIdAndUserIdService findRunSessionByIdAndUserIdService;
	private final S3Service s3Service;
	private final RedisStreamPublisher redisStreamPublisher;

	@Transactional
	public RunSession complete(Long sessionId, Long userId, CompleteRunRequest request, MultipartFile mapImage) {
		RunSession runSession = findRunSessionByIdAndUserIdService.find(sessionId, userId);

		String uploadedMapImage = s3Service.uploadImage(mapImage);
		runSession.complete(
			request.distance().total(),
			request.duration().total(),
			request.pace().avg(),
			request.pace().max().value(),
			request.pace().max().latitude(),
			request.pace().max().longitude(),
			request.cadence().avg(),
			request.cadence().max().value(),
			uploadedMapImage
		);

		// 인증 독촉 알림 이벤트 발행 (23시간 후)
		FeedReminderRequestedEvent feedReminderEvent = FeedReminderRequestedEvent.builder()
			.userId(userId)
			.runSessionId(runSession.getId())
			.build();

		redisStreamPublisher.publishAfterCommit(feedReminderEvent);
		log.info("FeedReminderRequestedEvent published: userId={}, runSessionId={}", userId, runSession.getId());

		// 러닝 독촉 알림 이벤트 발행 (7일 후)
		RunningProgressReminderRequestedEvent runningProgressEvent = RunningProgressReminderRequestedEvent.builder()
			.userId(userId)
			.runSessionId(runSession.getId())
			.build();

		redisStreamPublisher.publishAfterCommit(runningProgressEvent);
		log.info("RunningProgressReminderRequestedEvent published: userId={}, runSessionId={}", userId,
			runSession.getId());

		return runSession;
	}

	public RunSessionResponse toResponse(RunSession runSession) {
		return new RunSessionResponse(
			runSession.getId(),
			runSession.getCreatedAt(),
			runSession.getUpdatedAt(),
			runSession.getFinishedAt(),
			runSession.getDistanceTotal(),
			runSession.getDurationTotal(),
			runSession.getPaceAvg(),
			runSession.getPaceMax(),
			runSession.getPaceMaxLatitude(),
			runSession.getPaceMaxLongitude(),
			runSession.getCadenceAvg(),
			runSession.getCadenceMax(),
			runSession.getMapImageUrl()
		);
	}
}
