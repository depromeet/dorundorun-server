package com.sixpack.dorundorun.feature.auth.application;

import org.springframework.stereotype.Service;

import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.infra.redis.token.RedisTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService {

	private final RedisTokenRepository redisTokenRepository;

	public void logout(User user) {
		Long userId = user.getId();
		redisTokenRepository.delete(userId);
	}
}
