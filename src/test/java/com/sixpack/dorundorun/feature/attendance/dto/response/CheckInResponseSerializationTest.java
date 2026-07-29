package com.sixpack.dorundorun.feature.attendance.dto.response;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

@DisplayName("CheckInResponse 직렬화 테스트")
class CheckInResponseSerializationTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("isFirstCheckInToday가 접두어(is)가 벗겨지지 않고 그대로 직렬화된다")
	void serialize_keepsIsPrefixOnBooleanField() throws Exception {
		CheckInResponse response = new CheckInResponse(5, 12, true);

		String json = objectMapper.writeValueAsString(response);

		assertThat(json).contains("\"streakCount\":5");
		assertThat(json).contains("\"longestStreak\":12");
		assertThat(json).contains("\"isFirstCheckInToday\":true");
		assertThat(json).doesNotContain("\"firstCheckInToday\"");
	}
}
