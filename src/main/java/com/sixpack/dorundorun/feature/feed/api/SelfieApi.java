package com.sixpack.dorundorun.feature.feed.api;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.feature.feed.dto.request.CreateSelfieRequest;
import com.sixpack.dorundorun.feature.feed.dto.request.FeedListRequest;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieReactionRequest;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieWeekListRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieReactionResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieWeekResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;
import com.sixpack.dorundorun.global.response.PaginationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "[인증피드 관련]")
public interface SelfieApi {

	@Operation(summary = "유저의 인증피드 목록 조회",
		description = "currentDate를 기준으로 유저의 인증피드(selfie feed) 목록을 조회합니다. userId가 있으면 해당 유저의 페이지(내 페이지 또는 친구 페이지) 데이터 반환, 없으면 해당 날짜의 친구들 인증 피드 반환")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "인증목록 조회에 성공하였습니다"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "유저를 찾을 수 없음")
	})
	DorunResponse<PaginationResponse<SelfieFeedResponse>> getFeedsByDate(
		@ParameterObject @ModelAttribute FeedListRequest request,
		@Parameter(hidden = true) @CurrentUser User user
	);

	@Operation(summary = "인증피드 업로드",
		description = "셀피 인증피드를 업로드합니다. 셀피 이미지는 선택사항이며, 없을 경우 맵 이미지가 대신 표시됩니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "인증 업로드에 성공하였습니다"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	DorunResponse<Void> createSelfie(
		@Parameter(hidden = true) @CurrentUser User user,

		@Parameter(
			description = "인증 데이터",
			required = true,
			schema = @io.swagger.v3.oas.annotations.media.Schema(
				implementation = CreateSelfieRequest.class,
				type = "string",
				format = "json"
			)
		)
		@RequestPart(value = "data") String dataJson,

		@Parameter(description = "셀피 이미지 (선택사항)", required = false)
		@RequestPart(value = "selfieImage", required = false) MultipartFile selfieImage
	);

	@Operation(summary = "주차별 친구들의 인증수 조회",
		description = "기간(startDate ~ endDate) 동안 날짜별 친구들의 인증 수를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "주차별 친구들 인증수 조회에 성공하였습니다"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	DorunResponse<SelfieWeekResponse> getWeeklySelfies(
		@Parameter(hidden = true) @CurrentUser User user,
		@ParameterObject @ModelAttribute SelfieWeekListRequest request
	);

	@Operation(summary = "친구 인증 반응 남기기",
		description = "특정 인증에 이모지 반응을 추가하거나 취소합니다. 사용자가 누른 반응에 대해 이미 동일한 이모지 이력이 있으면 취소(-1), 없으면 새로 추가(+1) 됩니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "인증 반응 남기기에 성공하였습니다 / 인증 반응 취소에 성공하였습니다"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "셀피를 찾을 수 없습니다")
	})
	DorunResponse<SelfieReactionResponse> reactToSelfie(
		@Parameter(hidden = true) @CurrentUser User user,
		@Valid @RequestBody SelfieReactionRequest request
	);
}
