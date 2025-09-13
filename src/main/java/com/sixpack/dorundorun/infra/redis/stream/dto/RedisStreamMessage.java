package com.sixpack.dorundorun.infra.redis.stream.dto;

import com.sixpack.dorundorun.infra.redis.stream.event.RedisStreamEvent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedisStreamMessage implements Serializable {
	private String id;
	private String type;
	private Object payload;
	private LocalDateTime timestamp;
	private String source;

	public static RedisStreamMessage of(RedisStreamEvent event) {
		return RedisStreamMessage.builder()
				.type(event.type())
				.payload(event)
				.timestamp(LocalDateTime.now())
				.build();
	}

	public <T> T getPayloadAs(ObjectMapper om, Class<T> type) {
		return om.convertValue(payload, type);
	}
}
