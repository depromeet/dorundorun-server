package com.sixpack.dorundorun.feature.run.api;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.run.application.CompleteRunSessionService;
import com.sixpack.dorundorun.global.exception.CustomException;
import com.sixpack.dorundorun.global.exception.GlobalErrorCode;
import com.sixpack.dorundorun.feature.run.application.CreateRunSessionService;
import com.sixpack.dorundorun.feature.run.application.FindAllRunSessionsService;
import com.sixpack.dorundorun.feature.run.application.FindRunSessionDetailService;
import com.sixpack.dorundorun.feature.run.application.SaveRunSegmentService;
import com.sixpack.dorundorun.feature.run.domain.RunSegment;
import com.sixpack.dorundorun.feature.run.domain.RunSession;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RunController implements RunApi {

	private final CreateRunSessionService createRunSessionService;
	private final SaveRunSegmentService saveRunSegmentService;
	private final CompleteRunSessionService completeRunSessionService;
	private final FindAllRunSessionsService findRunSessionListService;
	private final FindRunSessionDetailService findRunSessionDetailService;
	private final ObjectMapper objectMapper;

	@PostMapping("/api/runs/sessions/start")
	public DorunResponse<SaveRunSessionResponse> start(@CurrentUser User user) {
		RunSession runSession = createRunSessionService.create(user.getId());
		SaveRunSessionResponse response = new SaveRunSessionResponse(runSession.getId());
		return DorunResponse.success("러닝 세션이 성공적으로 시작되었습니다.", response);
	}

	@PostMapping("/api/runs/sessions/{sessionId}/segments")
	public DorunResponse<SaveRunSegmentResponse> saveRunSegments(
		@PathVariable Long sessionId,
		@CurrentUser User user,
		@Valid @RequestBody SaveRunSegmentRequest segmentData
	) {
		RunSegment runSegment = saveRunSegmentService.save(sessionId, user.getId(), segmentData);
		SaveRunSegmentResponse response = new SaveRunSegmentResponse(
			runSegment.getId(),
			runSegment.getData().segments().size()
		);

		return DorunResponse.created("러닝 데이터가 성공적으로 저장되었습니다.", response);
	}

	@PostMapping(value = "/api/runs/sessions/{sessionId}/complete",
		consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DorunResponse<RunSessionResponse> completeRunSession(
		@PathVariable Long sessionId,
		@CurrentUser User user,
		@RequestPart(value = "data") String dataJson,
		@RequestPart(value = "mapImage") MultipartFile mapImage
	) {
		try {
			// JSON 문자열을 객체로 변환
			CompleteRunRequest data = objectMapper.readValue(dataJson, CompleteRunRequest.class);

			RunSession completedSession = completeRunSessionService.complete(
				sessionId,
				user.getId(),
				data,
				mapImage
			);
			RunSessionResponse response = completeRunSessionService.toResponse(completedSession);

			return DorunResponse.success("러닝 세션이 성공적으로 완료되었습니다.", response);
		} catch (JsonProcessingException e) {
			throw new CustomException(GlobalErrorCode.INVALID_INPUT, "잘못된 JSON 형식입니다.");
		}
	}

	@GetMapping("/api/runs/sessions")
	public DorunResponse<List<RunSessionListResponse>> getRunSessions(
		@CurrentUser User user,
		@ModelAttribute RunSessionListRequest request
	) {
		List<RunSessionListResponse> runSessions = findRunSessionListService.find(user.getId(), request);
		return DorunResponse.success("러닝 기록 조회가 성공적으로 완료되었습니다.", runSessions);
	}

	@GetMapping("/api/runs/sessions/{sessionId}")
	public DorunResponse<RunSessionDetailResponse> getRunSessionDetail(
		@PathVariable Long sessionId,
		@CurrentUser User user
	) {
		RunSessionDetailResponse runSessionDetail = findRunSessionDetailService.find(sessionId, user.getId());
		return DorunResponse.success("러닝 상세 기록 조회가 성공적으로 완료되었습니다.", runSessionDetail);
	}
}
