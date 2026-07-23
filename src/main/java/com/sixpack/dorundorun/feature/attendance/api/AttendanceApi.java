package com.sixpack.dorundorun.feature.attendance.api;

import com.sixpack.dorundorun.feature.attendance.dto.response.CheckInResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[출석 체크 관련]")
public interface AttendanceApi {

	@Operation(summary = "출석 체크인", description = "로그인된 사용자의 오늘 출석을 기록하고, 갱신된 연속 방문 일수를 반환합니다. 하루에 여러 번 호출해도 안전합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "체크인 성공")
	})
	DorunResponse<CheckInResponse> checkIn(
		@Parameter(hidden = true) @CurrentUser User user
	);
}
