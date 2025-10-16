package com.sixpack.dorundorun.feature.run.api;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.feature.run.dto.request.CompleteRunRequest;
import com.sixpack.dorundorun.feature.run.dto.request.RunSessionListRequest;
import com.sixpack.dorundorun.feature.run.dto.request.SaveRunSegmentRequest;
import com.sixpack.dorundorun.feature.run.dto.response.RunSessionDetailResponse;
import com.sixpack.dorundorun.feature.run.dto.response.RunSessionListResponse;
import com.sixpack.dorundorun.feature.run.dto.response.RunSessionResponse;
import com.sixpack.dorundorun.feature.run.dto.response.SaveRunSegmentResponse;
import com.sixpack.dorundorun.feature.run.dto.response.SaveRunSessionResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "[달리기 관련]")
public interface RunApi {

	@Operation(summary = "러닝 시작", description = "러닝 세션을 시작하고 세션아이디를 발급합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "러닝 시작 성공"),
		@ApiResponse(responseCode = "400", description = "이미 진행중인 러닝이 존재합니다. [runSessionId: %s]")
	})
	DorunResponse<SaveRunSessionResponse> start(@Parameter(hidden = true) @CurrentUser User user);

	@Operation(summary = "러닝 데이터 저장", description = "러닝 중 5분마다 수집된 데이터를 저장합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "러닝 데이터 저장 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
	})
	DorunResponse<SaveRunSegmentResponse> saveRunSegments(
		@PathVariable Long sessionId,
		@Parameter(hidden = true) @CurrentUser User user,
		@Valid @RequestBody SaveRunSegmentRequest segmentData
	);

	@Operation(summary = "러닝 종료", description = "러닝 세션을 완료하고 최종 데이터를 저장합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "러닝 종료 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
	})
	DorunResponse<RunSessionResponse> completeRunSession(
		@Parameter(description = "세션 ID", required = true)
		@PathVariable Long sessionId,

		@Parameter(hidden = true)
		@CurrentUser User user,

		@Parameter(
			description = "러닝 데이터",
			required = true,
			schema = @Schema(
				implementation = CompleteRunRequest.class,
				type = "string",
				format = "json"
			)
		)
		@RequestPart(value = "data") String dataJson,

		@Parameter(description = "지도 이미지", required = true)
		@RequestPart(value = "mapImage") MultipartFile mapImage
	);

	@Operation(summary = "러닝 기록 조회", description = "완료된 러닝 세션 목록을 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "러닝 기록 조회 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	DorunResponse<List<RunSessionListResponse>> getRunSessions(
		@Parameter(hidden = true) @CurrentUser User user,
		@ParameterObject @ModelAttribute RunSessionListRequest request
	);

	@Operation(summary = "러닝 상세 기록 조회", description = "특정 러닝 세션의 상세 정보를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "러닝 상세 기록 조회 성공"),
		@ApiResponse(responseCode = "404", description = "해당 러닝 세션을 찾을 수 없습니다.")
	})
	DorunResponse<RunSessionDetailResponse> getRunSessionDetail(
		@Parameter(description = "세션 ID", required = true) @PathVariable Long sessionId,
		@Parameter(hidden = true) @CurrentUser User user
	);
}
