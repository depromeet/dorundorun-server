package com.sixpack.dorundorun.feature.user.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.user.dto.response.UserResponse;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[2. 사용자 관련]")
public interface UserApi {

	@Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 정보를 조회합니다.")
	@ApiResponse(responseCode = "200", description = "조회 성공")
	DorunResponse<UserResponse> getMyInfo(
		@Parameter(hidden = true) @CurrentUser User currentUser
	);

	// (미완) 온보딩 정보 저장 API
	@Operation(summary = "온보딩 정보 저장 (미완)", description = "회원가입 후 마케팅 수신 동의 및 위치 정보 권한 동의 정보를 저장합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "온보딩 정보 저장 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
	})
	DorunResponse<Void> saveOnboardingInfo(
		@Parameter(description = "사용자 ID", required = true)
		@PathVariable Long userId,
		@Parameter(description = "유저 ID", required = true)
		@RequestHeader("X-User-Id") String userIdHeader
		// TODO: OnboardingRequest DTO 추가 예정
		// @Valid @RequestBody OnboardingRequest request
	);

	// (미완) 유저 Goal 저장 API
	@Operation(summary = "유저 Goal 저장 (미완)", description = "사용자의 러닝 목표를 설정합니다. 이 때 세부목표까지 자동으로 생성됩니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "목표 생성 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
	})
	DorunResponse<Void> createUserGoal(
		@Parameter(description = "사용자 ID", required = true)
		@PathVariable Long userId,
		@Parameter(description = "유저 ID", required = true)
		@RequestHeader("X-User-Id") String userIdHeader
		// TODO: CreateGoalRequest DTO 추가 예정
		// @Valid @RequestBody CreateGoalRequest request
	);
}
