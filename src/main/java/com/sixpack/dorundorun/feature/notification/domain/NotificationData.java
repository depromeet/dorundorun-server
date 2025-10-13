package com.sixpack.dorundorun.feature.notification.domain;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

/**
 * 알림 데이터를 담는 VO (Value Object)
 * JSON 형태로 저장되며, 알림 타입과 트리거에 따라 다양한 데이터를 포함할 수 있습니다.
 */
@Getter
public class NotificationData {

	private final String title;
	private final String message;
	private final Map<String, Object> additionalData;

	@Builder
	@JsonCreator
	public NotificationData(
		@JsonProperty("title") String title,
		@JsonProperty("message") String message,
		@JsonProperty("additionalData") Map<String, Object> additionalData) {
		this.title = title;
		this.message = message;
		this.additionalData = additionalData;
	}
}
