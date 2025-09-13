package com.sixpack.dorundorun.feature.run.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.sixpack.dorundorun.feature.run.domain.RunSegmentData;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RunController implements RunApi {

	@PostMapping("/api/runs/sessions/start")
	public ResponseEntity<Void> startRunSession(@RequestHeader("X-User-Id") String userId) {
		// TODO: 러닝 세션 시작 로직 구현
		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@PostMapping("/api/runs/sessions/{sessionId}/segments")
	public ResponseEntity<Void> saveRunSegments(
		@PathVariable Long sessionId,
		@RequestHeader("X-User-Id") String userId,
		@Valid @RequestBody List<RunSegmentData> segmentData
	) {
		// TODO: 러닝 데이터 저장 로직 구현
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@PostMapping("/api/runs/sessions/{sessionId}/complete")
	public ResponseEntity<Void> completeRunSession(
		@PathVariable Long sessionId,
		@RequestHeader("X-User-Id") String userId
		// TODO: 러닝 완료 요청 DTO 추가 예정
		// @Valid @RequestBody CompleteRunRequest request
	) {
		// TODO: 러닝 세션 완료 로직 구현
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}