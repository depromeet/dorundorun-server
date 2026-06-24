package com.sixpack.dorundorun.feature.user.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sixpack.dorundorun.feature.user.dao.UserJpaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvalidateDeviceTokenService {

	private final UserJpaRepository userJpaRepository;

	// FCM이 UNREGISTERED를 반환한 죽은 토큰을 제거한다.
	// 같은 트랜잭션 안에서 다시 조회해서 영속 상태로 만든 뒤 수정해야 더티체킹이 적용된다.
	@Transactional
	public void invalidate(Long userId) {
		userJpaRepository.findById(userId)
			.ifPresent(user -> {
				user.updateDeviceToken("");
				log.info("Invalidated stale device token: userId={}", userId);
			});
	}
}
