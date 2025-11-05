package com.sixpack.dorundorun.feature.friend.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.friend.event.CheerRequestedEvent;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.redis.stream.publisher.RedisStreamPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheerFriendService {

	private final FindUserByIdService findUserByIdService;
	private final RedisStreamPublisher redisStreamPublisher;

	@Transactional
	public String cheer(Long userId, Long friendUserId) {

		User friendUser = findUserByIdService.find(friendUserId);

		CheerRequestedEvent event = CheerRequestedEvent.builder()
			.cheererId(userId)
			.cheeringUserId(friendUserId)
			.build();

		redisStreamPublisher.publishAfterCommit(event);
		log.info("Cheer event published: cheererId={}, cheeringUserId={}", userId, friendUserId);

		return friendUser.getNickname();
	}
}
