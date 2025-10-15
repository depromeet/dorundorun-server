package com.sixpack.dorundorun.feature.run.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.sixpack.dorundorun.feature.run.application.CreateRunSessionService;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
import com.sixpack.dorundorun.feature.run.dto.request.CompleteRunRequest;
import com.sixpack.dorundorun.feature.run.dto.request.SaveRunSegmentRequest;
import com.sixpack.dorundorun.feature.run.dto.response.RunSessionResponse;
import com.sixpack.dorundorun.feature.run.dto.response.SaveRunSegmentResponse;
import com.sixpack.dorundorun.feature.run.dto.response.SaveRunSessionResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RunController implements RunApi {

	private final CreateRunSessionService createRunSessionService;

	@PostMapping("/api/runs/sessions/start")
	public DorunResponse<SaveRunSessionResponse> start(@CurrentUser User user) {
		RunSession runSession = createRunSessionService.create(user.getId());
		SaveRunSessionResponse response = new SaveRunSessionResponse(runSession.getId());
		return DorunResponse.success("러닝 세션이 성공적으로 시작되었습니다.", response);
	}

	@PostMapping("/api/runs/sessions/{sessionId}/segments")
	public DorunResponse<SaveRunSegmentResponse> saveRunSegments(
		@PathVariable Long sessionId,
		@RequestHeader("X-User-Id") String userId,
		@Valid @RequestBody SaveRunSegmentRequest segmentData
	) {
		// TODO: 러닝 데이터 저장 로직 구현
		// 임시 더미 데이터
		SaveRunSegmentResponse response = new SaveRunSegmentResponse(
			456L,
			5
		);

		return DorunResponse.created("러닝 데이터가 성공적으로 저장되었습니다.", response);
	}

	@PostMapping("/api/runs/sessions/{sessionId}/complete")
	public DorunResponse<RunSessionResponse> completeRunSession(
		@PathVariable Long sessionId,
		@RequestHeader("X-User-Id") String userId,
		@Valid @RequestBody CompleteRunRequest request
	) {
		// TODO: 러닝 세션 완료 로직 구현
		// 임시 더미 데이터
		RunSessionResponse response = new RunSessionResponse(
			sessionId,                                    // id
			java.time.LocalDateTime.now().minusHours(1),  // createdAt
			java.time.LocalDateTime.now(),                // updatedAt
			1L,                                           // goalPlanId
			java.time.LocalDateTime.now(),                // clearedAt (현재 완료)
			5000L,                                        // totalDistance (5km)
			1800L,                                        // totalDuration (30분)
			360L,                                         // avgPace (6분/km)
			170,                                          // avgCadence
			185                                           // maxCadence
		);

		return DorunResponse.success("러닝 세션이 성공적으로 완료되었습니다.", response);
	}
}
