package com.sixpack.dorundorun.feature.feed.api;

import java.time.LocalDate;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.sixpack.dorundorun.feature.feed.dto.request.CreateSelfieRequest;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieReactionRequest;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieWeekListRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieReactionResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieWeekResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "[5. 셀피 관련]")
public interface SelfieApi {

	@Operation(summary = "[인증] 유저의 인증 목록 조회", 
		description = "특정 날짜(currentDate)를 기준으로 유저의 셀피(인증) 목록을 조회합니다. userId가 있으면 해당 유저의 페이지(내 페이지 또는 친구 페이지) 데이터 반환, 없으면 해당 날짜의 친구들 인증 피드 반환")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "인증목록 조회에 성공하였습니다"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
	})
	DorunResponse<SelfieFeedResponse> getSelfiesByDate(
		@Parameter(description = "조회 기준 날짜 (YYYY-MM-DD)") @RequestParam(required = false) LocalDate currentDate,
		@Parameter(description = "조회할 유저 ID") @RequestParam(required = false) Long userId,
		@Parameter(hidden = true) @CurrentUser User user
	);

	@Operation(summary = "[인증] 인증 업로드", 
		description = "셀피 인증을 업로드합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "인증 업로드에 성공하였습니다"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	DorunResponse<Void> createSelfie(
		@Parameter(hidden = true) @CurrentUser User user,
		@Valid @RequestBody CreateSelfieRequest request
	);

	@Operation(summary = "[인증] 주차별 친구들의 인증수 조회", 
		description = "기간(startDate ~ endDate) 동안 날짜별 친구들의 인증 수를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "주차별 친구들 인증수 조회에 성공하였습니다"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	DorunResponse<SelfieWeekResponse> getWeeklySelfies(
		@Parameter(hidden = true) @CurrentUser User user,
		@ParameterObject @ModelAttribute SelfieWeekListRequest request
	);

	@Operation(summary = "[인증] 친구 인증 반응 남기기", 
		description = "특정 셀피에 이모지 반응을 추가하거나 취소합니다. 이미 동일한 이모지 반응이 있으면 취소(REMOVED), 없으면 새로 추가(ADDED)")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "인증 반응 남기기에 성공하였습니다 / 인증 반응 취소에 성공하였습니다"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "403", description = "자신의 게시물에는 반응할 수 없습니다"),
		@ApiResponse(responseCode = "404", description = "셀피를 찾을 수 없습니다")
	})
	DorunResponse<SelfieReactionResponse> reactToSelfie(
		@Parameter(description = "셀피 ID", required = true) @PathVariable Long selfieId,
		@Parameter(hidden = true) @CurrentUser User user,
		@Valid @RequestBody SelfieReactionRequest request
	);
}
