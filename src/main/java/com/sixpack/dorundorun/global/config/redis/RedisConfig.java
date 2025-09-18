package com.sixpack.dorundorun.global.config.redis;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.global.config.redis.stream.RedisStreamProperties;

@Configuration
@EnableConfigurationProperties(RedisStreamProperties.class)
public class RedisConfig {

	@Bean
	public RedisTemplate<String, Object> redisTemplate(
		RedisConnectionFactory connectionFactory,
		ObjectMapper objectMapper
	) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// Key는 String으로 직렬화
		template.setKeySerializer(new StringRedisSerializer());
		template.setHashKeySerializer(new StringRedisSerializer());

		// Value는 JSON으로 직렬화
		template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
		template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

		template.afterPropertiesSet();
		return template;
	}
}
