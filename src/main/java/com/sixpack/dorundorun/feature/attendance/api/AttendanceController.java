package com.sixpack.dorundorun.feature.attendance.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sixpack.dorundorun.feature.attendance.application.CheckInService;
import com.sixpack.dorundorun.feature.attendance.dto.response.CheckInResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AttendanceController implements AttendanceApi {

	private final CheckInService checkInService;

	@PostMapping("/api/attendance/check-in")
	public DorunResponse<CheckInResponse> checkIn(
		@CurrentUser User user
	) {
		CheckInResponse response = checkInService.checkIn(user);
		return DorunResponse.success("출석 체크가 완료되었습니다.", response);
	}
}
