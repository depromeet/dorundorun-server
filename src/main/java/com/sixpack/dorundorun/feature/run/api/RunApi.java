package com.sixpack.dorundorun.feature.run.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "[4. 달리기 관련]")

public interface RunApi {

	@Operation(summary = "러닝 시작", description = "러닝 세션을 시작하고 세션아이디를 발급합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "러닝 시작 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	ResponseEntity<Void> startRunSession(
		@Parameter(description = "유저 ID", required = true)
		@RequestHeader("X-User-Id") String userId
	);

	@Operation(summary = "러닝 데이터 저장", description = "러닝 중 5분마다 수집된 데이터를 저장합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "201", description = "러닝 데이터 저장 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
	})
	ResponseEntity<Void> saveRunSegments(
		@Parameter(description = "세션 ID", required = true)
		@PathVariable Long sessionId,
		@Parameter(description = "유저 ID", required = true)
		@RequestHeader("X-User-Id") String userId,
		@Valid @RequestBody List<RunSegmentData> segmentData
	);

	@Operation(summary = "러닝 종료", description = "러닝 세션을 완료하고 최종 데이터를 저장합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "러닝 종료 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
	})
	ResponseEntity<Void> completeRunSession(
		@Parameter(description = "세션 ID", required = true)
		@PathVariable Long sessionId,
		@Parameter(description = "유저 ID", required = true)
		@RequestHeader("X-User-Id") String userId
		// TODO: 러닝 완료 요청 DTO 추가 예정
		// @Valid @RequestBody CompleteRunRequest request
	);
}