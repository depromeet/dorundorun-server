package com.sixpack.dorundorun.feature.feed.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.feed.application.CreateSelfieService;
import com.sixpack.dorundorun.feature.feed.application.DeleteSelfieService;
import com.sixpack.dorundorun.feature.feed.application.FindAllWeeklySelfiesService;
import com.sixpack.dorundorun.feature.feed.application.FindFeedByIdService;
import com.sixpack.dorundorun.feature.feed.application.FindSelfieUsersByDateService;
import com.sixpack.dorundorun.feature.feed.application.FindSelfiesByDateService;
import com.sixpack.dorundorun.feature.feed.application.UpdateSelfieService;
import com.sixpack.dorundorun.feature.feed.dto.request.CreateSelfieRequest;
import com.sixpack.dorundorun.feature.feed.dto.request.FeedListRequest;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieReactionRequest;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieUsersRequest;
import com.sixpack.dorundorun.feature.feed.dto.request.SelfieWeekListRequest;
import com.sixpack.dorundorun.feature.feed.dto.request.UpdateSelfieRequest;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieFeedResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieReactionResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieUsersResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.SelfieWeekResponse;
import com.sixpack.dorundorun.feature.feed.dto.response.UpdateSelfieResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/selfie")
@RequiredArgsConstructor
public class SelfieController implements SelfieApi {

	private final FindSelfiesByDateService findSelfiesByDateService;
	private final CreateSelfieService createSelfieService;
	private final UpdateSelfieService updateSelfieService;
	private final DeleteSelfieService deleteSelfieService;
	private final FindAllWeeklySelfiesService findAllWeeklySelfiesService;
	private final FindSelfieUsersByDateService findSelfieUsersByDateService;
	private final FindFeedByIdService findFeedByIdService;
	private final ObjectMapper objectMapper;

	@Override
	@GetMapping("/feeds")
	public DorunResponse<SelfieFeedResponse> getFeedsByDate(
		@ModelAttribute FeedListRequest request,
		@CurrentUser User user
	) {
		SelfieFeedResponse response = findSelfiesByDateService.find(user, request);
		return DorunResponse.success("인증목록 조회에 성공하였습니다", response);
	}

	@Override
	@PostMapping(value = "/feeds", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DorunResponse<Void> createSelfie(
		@CurrentUser User user,
		@RequestPart("data") String dataJson,
		@RequestPart(value = "selfieImage", required = false) MultipartFile selfieImage
	) {
		try {
			// JSON 문자열을 객체로 변환
			CreateSelfieRequest data = objectMapper.readValue(dataJson, CreateSelfieRequest.class);

			createSelfieService.create(user, data, selfieImage);

			return DorunResponse.success("인증 업로드에 성공하였습니다");
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("잘못된 JSON 형식입니다.", e);
		}
	}

	@Override
	@GetMapping("/week")
	public DorunResponse<SelfieWeekResponse> getWeeklySelfies(
		@CurrentUser User user,
		@ModelAttribute SelfieWeekListRequest request
	) {
		SelfieWeekResponse response = findAllWeeklySelfiesService.execute(user, request);
		return DorunResponse.success("주차별 친구들 인증수 조회에 성공하였습니다", response);
	}

	@Override
	@PostMapping("/feeds/reaction")
	public DorunResponse<SelfieReactionResponse> reactToSelfie(
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

	@Override
	@GetMapping("/users")
	public DorunResponse<SelfieUsersResponse> getSelfieUsersByDate(
		@CurrentUser User user,
		@Valid @ModelAttribute SelfieUsersRequest request
	) {
		SelfieUsersResponse response = findSelfieUsersByDateService.find(user, request);
		return DorunResponse.success("셀피 유저 목록 조회에 성공하였습니다", response);
	}

	@Override
	@PutMapping(value = "/feeds/{feedId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DorunResponse<UpdateSelfieResponse> updateSelfie(
		@PathVariable Long feedId,
		@CurrentUser User user,
		@RequestPart("data") String dataJson,
		@RequestPart(value = "selfieImage", required = false) MultipartFile selfieImage
	) {
		try {
			UpdateSelfieRequest data = objectMapper.readValue(dataJson, UpdateSelfieRequest.class);
			String selfieImageUrl = updateSelfieService.update(feedId, user, data, selfieImage);
			return DorunResponse.success("셀피 수정에 성공하였습니다", new UpdateSelfieResponse(selfieImageUrl));
		} catch (JsonProcessingException e) {
			throw new IllegalArgumentException("잘못된 JSON 형식입니다.", e);
		}
	}

	@Override
	@DeleteMapping("/feeds/{feedId}")
	public DorunResponse<Void> deleteSelfie(
		@PathVariable Long feedId,
		@CurrentUser User user
	) {
		deleteSelfieService.delete(feedId, user);
		return DorunResponse.success("셀피 삭제에 성공하였습니다");
	}

	@Override
	@GetMapping("/feeds/{feedId}")
	public DorunResponse<SelfieFeedResponse.FeedItem> getFeedById(
		@PathVariable Long feedId,
		@CurrentUser User user
	) {
		SelfieFeedResponse.FeedItem response = findFeedByIdService.find(feedId, user);
		return DorunResponse.success("인증피드 조회에 성공하였습니다", response);
	}
}
