package com.sixpack.dorundorun.feature.feed.api;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sixpack.dorundorun.feature.feed.dto.request.CreateSelfieRequest;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieReactionRequest;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieWeekListRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieReactionResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieWeekResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/selfie")
@RequiredArgsConstructor
public class SelfieController implements SelfieApi {

	// TODO: 서비스 의존성 주입 예정
	// private final GetSelfiesByDateService getSelfiesByDateService;
	// private final CreateSelfieService createSelfieService;
	// private final GetWeeklySelfiesService getWeeklySelfiesService;
	// private final ReactToSelfieService reactToSelfieService;

	@Override
	@GetMapping("/feeds")
	public DorunResponse<SelfieFeedResponse> getSelfiesByDate(
		@RequestParam(required = false) LocalDate currentDate,
		@RequestParam(required = false) Long userId,
		@CurrentUser User user
	) {
		// TODO: 서비스 로직 구현 예정
		// SelfieFeedResponse response = getSelfiesByDateService.execute(currentDate, userId, user);
		// return DorunResponse.success("인증목록 조회에 성공하였습니다", response);
		return DorunResponse.success("인증목록 조회에 성공하였습니다", null);
	}

	@Override
	@PostMapping("/feeds")
	public DorunResponse<Void> createSelfie(
		@CurrentUser User user,
		@Valid @RequestBody CreateSelfieRequest request
	) {
		// TODO: 서비스 로직 구현 예정
		// createSelfieService.execute(user, request);
		// return DorunResponse.success("인증 업로드에 성공하였습니다");
		return DorunResponse.success("인증 업로드에 성공하였습니다");
	}

	@Override
	@GetMapping("/week")
	public DorunResponse<SelfieWeekResponse> getWeeklySelfies(
		@CurrentUser User user,
		@ModelAttribute SelfieWeekListRequest request
	) {
		// TODO: 서비스 로직 구현 예정
		// SelfieWeekResponse response = getWeeklySelfiesService.execute(user, request);
		// return DorunResponse.success("주차별 친구들 인증수 조회에 성공하였습니다", response);
		return DorunResponse.success("주차별 친구들 인증수 조회에 성공하였습니다", null);
	}

	@Override
	@PostMapping("/feeds/{selfieId}/reaction")
	public DorunResponse<SelfieReactionResponse> reactToSelfie(
		@PathVariable Long selfieId,
		@CurrentUser User user,
		@Valid @RequestBody SelfieReactionRequest request
	) {
		// TODO: 서비스 로직 구현 예정
		// SelfieReactionResponse response = reactToSelfieService.execute(selfieId, user, request);
		// String message = response.getAction().equals("ADDED") 
		// 	? "인증 반응 남기기에 성공하였습니다" 
		// 	: "인증 반응 취소에 성공하였습니다";
		// return DorunResponse.success(message, response);
		return DorunResponse.success("인증 반응 남기기에 성공하였습니다", null);
	}
}
