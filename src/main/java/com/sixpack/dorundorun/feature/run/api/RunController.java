package com.sixpack.dorundorun.feature.run.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.sixpack.dorundorun.feature.run.dto.request.CompleteRunRequest;
import com.sixpack.dorundorun.feature.run.dto.request.SaveRunSegmentRequest;
import com.sixpack.dorundorun.feature.run.dto.request.SaveRunSessionRequest;
import com.sixpack.dorundorun.feature.run.dto.response.RunSessionResponse;
import com.sixpack.dorundorun.feature.run.dto.response.SaveRunSegmentResponse;
import com.sixpack.dorundorun.feature.run.dto.response.SaveRunSessionResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RunController implements RunApi {

	@PostMapping("/api/runs/sessions/start")
	public ResponseEntity<SaveRunSessionResponse> startRunSession(@RequestHeader("X-User-Id") String userId,
		@Valid @RequestBody SaveRunSessionRequest segmentData) {
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@PostMapping("/api/runs/sessions/{sessionId}/segments")
	public ResponseEntity<SaveRunSegmentResponse> saveRunSegments(
		@PathVariable Long sessionId,
		@RequestHeader("X-User-Id") String userId,
		@Valid @RequestBody SaveRunSegmentRequest segmentData
	) {
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("/api/runs/sessions/{sessionId}/complete")
	public ResponseEntity<RunSessionResponse> completeRunSession(
		@PathVariable Long sessionId,
		@RequestHeader("X-User-Id") String userId,
		@Valid @RequestBody CompleteRunRequest request
	) {
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}