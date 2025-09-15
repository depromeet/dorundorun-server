package com.sixpack.dorundorun.feature.goal.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.sixpack.dorundorun.feature.goal.dto.response.GoalPlanResponse;
import com.sixpack.dorundorun.feature.goal.dto.response.LatestGoalResponse;
import com.sixpack.dorundorun.global.response.DorunResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[3. 목표 관련]")
public interface GoalApi {

	@Operation(summary = "최신 목표 조회", description = "가장 최신 목표(Goal)에 대한 정보를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "최신 목표 조회 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "목표를 찾을 수 없음")
	})
	DorunResponse<LatestGoalResponse> getLatestGoal(
		@Parameter(description = "유저 ID", required = true)
		@RequestHeader("X-User-Id") String userId
	);

	@Operation(summary = "세부 목표 조회", description = "세부 목표에 대한 정보를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "세부 목표 조회 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "세부 목표를 찾을 수 없음")
	})
	DorunResponse<GoalPlanResponse> getPlanDetail(
		@Parameter(description = "계획 ID", required = true)
		@PathVariable Long planId,
		@Parameter(description = "유저 ID", required = true)
		@RequestHeader("X-User-Id") String userId
	);

	@Operation(summary = "세부 목표 전체 조회 (미완)", description = "회차별 세부 목표(RunSession)를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "세션 목표 조회 성공"),
	})
	DorunResponse<Void> getGoalSessions(
		@Parameter(description = "목표 ID", required = true)
		@PathVariable Long goalId,
		@Parameter(description = "유저 ID", required = true)
		@RequestHeader("X-User-Id") String userId
	);

	// (미완) 전체 목표 수정 API
	@Operation(summary = "전체 목표 수정 (미완)", description = "기존 목표의 전체 정보를 수정합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "목표 수정 성공"),
	})
	DorunResponse<Void> updateGoal(
		@Parameter(description = "목표 ID", required = true)
		@PathVariable Long goalId,
		@Parameter(description = "유저 ID", required = true)
		@RequestHeader("X-User-Id") String userId
		// TODO: UpdateGoalRequest DTO 추가 예정
		// @Valid @RequestBody UpdateGoalRequest request
	);

	// (미완) 세부 목표 수정 API
	@Operation(summary = "세부 목표 수정 (미완)", description = "특정 세부 목표(GoalPlan)의 정보를 수정합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "세부 목표 수정 성공"),
	})
	DorunResponse<Void> updateGoalPlan(
		@Parameter(description = "계획 ID", required = true)
		@PathVariable Long planId,
		@Parameter(description = "유저 ID", required = true)
		@RequestHeader("X-User-Id") String userId
		// TODO: UpdateGoalPlanRequest DTO 추가 예정
		// @Valid @RequestBody UpdateGoalPlanRequest request
	);
}