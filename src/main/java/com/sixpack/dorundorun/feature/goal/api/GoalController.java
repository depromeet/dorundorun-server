package com.sixpack.dorundorun.feature.goal.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.sixpack.dorundorun.feature.goal.dto.response.GoalPlanResponse;
import com.sixpack.dorundorun.feature.goal.dto.response.LatestGoalResponse;
import com.sixpack.dorundorun.global.response.DorunResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GoalController implements GoalApi {

	@GetMapping("/api/runs/goals/latest")
	public DorunResponse<LatestGoalResponse> getLatestGoal(@RequestHeader("X-User-Id") String userId) {
		// TODO: 최신 목표 조회 로직 구현
		// 임시 더미 데이터
		LatestGoalResponse response = new LatestGoalResponse(
			1L,
			java.time.LocalDateTime.now(),
			java.time.LocalDateTime.now(),
			"하프마라톤 완주",
			java.time.LocalDate.of(2024, 1, 1),
			java.time.LocalDate.of(2024, 6, 30),
			null,
			360L,
			21097L,
			7200L,
			"WEEK",
			3
		);
		return DorunResponse.success("최신 목표 조회가 성공적으로 처리되었습니다.", response);
	}

	@GetMapping("/api/runs/goals/plans/{planId}")
	public DorunResponse<GoalPlanResponse> getPlanDetail(
		@PathVariable Long planId,
		@RequestHeader("X-User-Id") String userId
	) {
		// TODO: 세부 목표 조회 로직 구현
		// 임시 더미 데이터
		GoalPlanResponse response = new GoalPlanResponse(
			planId,
			java.time.LocalDateTime.now().minusDays(10), // createdAt
			java.time.LocalDateTime.now().minusDays(1),  // updatedAt
			java.time.LocalDateTime.now(),               // clearedAt
			1L,                                          // goalId
			360L,                                        // pace (6분/km)
			5000L,                                       // distance (5km)
			1800L,                                       // duration (30분)
			7L,                                          // roundCount
			12L                                          // totalRoundCount
		);
		return DorunResponse.success("세부 목표 조회가 성공적으로 처리되었습니다.", response);
	}

	@GetMapping("/api/runs/goals/{goalId}")
	public DorunResponse<Void> getGoalSessions(
		@PathVariable Long goalId,
		@RequestHeader("X-User-Id") String userId
	) {
		// TODO: 세션 목표 전체 조회 로직 구현
		return DorunResponse.success("세션 목표 조회가 성공적으로 처리되었습니다.");
	}

	// (미완) 전체 목표 수정 API
	@PutMapping("/api/goals/{goalId}")
	public DorunResponse<Void> updateGoal(
		@PathVariable Long goalId,
		@RequestHeader("X-User-Id") String userId
		// TODO: UpdateGoalRequest DTO 추가 예정
		// @Valid @RequestBody UpdateGoalRequest request
	) {
		// TODO: 전체 목표 수정 로직 구현
		return DorunResponse.success("목표가 성공적으로 수정되었습니다.");
	}

	// (미완) 세부 목표 수정 API
	@PutMapping("/api/goals/plans/{planId}")
	public DorunResponse<Void> updateGoalPlan(
		@PathVariable Long planId,
		@RequestHeader("X-User-Id") String userId
		// TODO: UpdateGoalPlanRequest DTO 추가 예정
		// @Valid @RequestBody UpdateGoalPlanRequest request
	) {
		// TODO: 세부 목표 수정 로직 구현
		return DorunResponse.success("세부 목표가 성공적으로 수정되었습니다.");
	}
}
