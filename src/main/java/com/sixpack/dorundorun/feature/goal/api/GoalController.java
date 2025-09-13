package com.sixpack.dorundorun.feature.goal.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.sixpack.dorundorun.feature.goal.dto.response.LatestGoalResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GoalController implements GoalApi {

	@GetMapping("/api/runs/goals/latest")
	public ResponseEntity<LatestGoalResponse> getLatestGoal(@RequestHeader("X-User-Id") String userId) {
		// TODO: 최신 목표 조회 로직 구현
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@GetMapping("/api/runs/goals/plans/{planId}")
	public ResponseEntity<Void> getPlanDetail(
		@PathVariable Long planId,
		@RequestHeader("X-User-Id") String userId
	) {
		// TODO: 세부 목표 조회 로직 구현
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@GetMapping("/api/runs/goals/{goalId}")
	public ResponseEntity<Void> getGoalSessions(
		@PathVariable Long goalId,
		@RequestHeader("X-User-Id") String userId
	) {
		// TODO: 세션 목표 전체 조회 로직 구현
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	// (미완) 전체 목표 수정 API
	@PutMapping("/api/goals/{goalId}")
	public ResponseEntity<Void> updateGoal(
		@PathVariable Long goalId,
		@RequestHeader("X-User-Id") String userId
		// TODO: UpdateGoalRequest DTO 추가 예정
		// @Valid @RequestBody UpdateGoalRequest request
	) {
		// TODO: 전체 목표 수정 로직 구현
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	// (미완) 세부 목표 수정 API
	@PutMapping("/api/goals/plans/{planId}")
	public ResponseEntity<Void> updateGoalPlan(
		@PathVariable Long planId,
		@RequestHeader("X-User-Id") String userId
		// TODO: UpdateGoalPlanRequest DTO 추가 예정
		// @Valid @RequestBody UpdateGoalPlanRequest request
	) {
		// TODO: 세부 목표 수정 로직 구현
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}