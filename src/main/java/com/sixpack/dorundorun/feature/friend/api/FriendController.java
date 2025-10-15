package com.sixpack.dorundorun.feature.friend.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sixpack.dorundorun.feature.friend.dto.request.AddFriendRequest;
import com.sixpack.dorundorun.feature.friend.dto.request.CheerFriendRequest;
import com.sixpack.dorundorun.feature.friend.dto.request.DeleteFriendsRequest;
import com.sixpack.dorundorun.feature.friend.dto.request.FriendRunningStatusRequest;
import com.sixpack.dorundorun.feature.friend.dto.response.AddFriendResponse;
import com.sixpack.dorundorun.feature.friend.dto.response.FriendRunningStatusResponse;
import com.sixpack.dorundorun.feature.friend.dto.response.GetMyCodeResponse;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;
import com.sixpack.dorundorun.global.response.PaginationResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FriendController implements FriendApi {

	@PostMapping("/api/friends/will-you-friend-me")
	public DorunResponse<AddFriendResponse> addFriend(
		@CurrentUser User user,
		@Valid @RequestBody AddFriendRequest request
	) {
		// TODO: 비즈니스 로직 구현
		return DorunResponse.success("친구 코드를 입력해서 추가하는 기능이 성공적으로 처리되었습니다.", null);
	}

	@GetMapping("/api/friends/will-you-friend-me")
	public DorunResponse<GetMyCodeResponse> getMyCode(
		@CurrentUser User user
	) {
		// TODO: 비즈니스 로직 구현
		return DorunResponse.success("친구에게 내 코드를 공유하는 기능이 성공적으로 처리되었습니다.", null);
	}

	@GetMapping("/api/friends/running/status")
	public DorunResponse<PaginationResponse<FriendRunningStatusResponse>> getFriendsRunningStatus(
		@CurrentUser User user,
		@ModelAttribute FriendRunningStatusRequest request
	) {
		// TODO: 비즈니스 로직 구현
		// Page<FriendRunningStatusResponse> page = service.getFriendsRunningStatus(user.getId(), request);
		// PaginationResponse<FriendRunningStatusResponse> response = PaginationResponse.of(
		//     page.getContent(),
		//     page.getNumber(),
		//     page.getSize(),
		//     page.getTotalElements()
		// );
		return DorunResponse.success("친구 러닝 현황 목록 조회 기능이 성공적으로 처리되었습니다.", null);
	}

	@PostMapping("/api/friends/delete")
	public DorunResponse<Void> deleteFriends(
		@CurrentUser User user,
		@Valid @RequestBody DeleteFriendsRequest request
	) {
		// TODO: 비즈니스 로직 구현
		return DorunResponse.success("친구 삭제 기능이 성공적으로 처리되었습니다.", null);
	}

	@PostMapping("/api/friends/reaction")
	public DorunResponse<Void> cheerFriend(
		@CurrentUser User user,
		@Valid @RequestBody CheerFriendRequest request
	) {
		// TODO: 비즈니스 로직 구현
		return DorunResponse.success("친구 응원하기 기능이 성공적으로 처리되었습니다.", null);
	}
}
