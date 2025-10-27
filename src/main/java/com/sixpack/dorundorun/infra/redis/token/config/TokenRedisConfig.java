package com.sixpack.dorundorun.infra.redis.token.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.infra.redis.token.RefreshToken;

/**
 * Token 전용 Redis 설정
 * - 기본 RedisTemplate과 분리하여 타입 안전성 보장
 * - RefreshToken 직렬화/역직렬화 전용
 */
@Configuration
public class TokenRedisConfig {

	@Bean
	public RedisTemplate<String, RefreshToken> tokenRedisTemplate(
		RedisConnectionFactory connectionFactory,
		ObjectMapper objectMapper
	) {
		RedisTemplate<String, RefreshToken> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// Key는 String으로 직렬화
		template.setKeySerializer(new StringRedisSerializer());

		// Value는 RefreshToken 전용 직렬화
		// ObjectMapper를 주입받아 사용 (기존 설정 재사용)
		Jackson2JsonRedisSerializer<RefreshToken> serializer =
			new Jackson2JsonRedisSerializer<>(objectMapper, RefreshToken.class);

		template.setValueSerializer(serializer);
		template.setHashValueSerializer(serializer);

		template.afterPropertiesSet();
		return template;
	}
}