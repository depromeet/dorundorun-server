package com.sixpack.dorundorun.feature.friend.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.sixpack.dorundorun.feature.friend.application.AddFriendService;
import com.sixpack.dorundorun.feature.friend.application.CheerFriendService;
import com.sixpack.dorundorun.feature.friend.application.DeleteFriendsService;
import com.sixpack.dorundorun.feature.friend.application.FindMyCodeService;
import com.sixpack.dorundorun.feature.friend.application.FriendsRunningStatusService;
import com.sixpack.dorundorun.feature.friend.domain.Friend;
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

	private final AddFriendService addFriendService;
	private final FindMyCodeService getMyCodeService;
	private final FriendsRunningStatusService getFriendsRunningStatusService;
	private final DeleteFriendsService deleteFriendsService;
	private final CheerFriendService cheerFriendService;

	@PostMapping("/api/friends/will-you-friend-me")
	public DorunResponse<AddFriendResponse> addFriend(
		@CurrentUser User user,
		@Valid @RequestBody AddFriendRequest request
	) {
		Friend friend = addFriendService.add(user.getId(), request.code());
		AddFriendResponse response = new AddFriendResponse(friend.getFriend().getId());
		return DorunResponse.success("친구가 성공적으로 추가되었습니다.", response);
	}

	@GetMapping("/api/friends/will-you-friend-me")
	public DorunResponse<GetMyCodeResponse> getMyCode(
		@CurrentUser User user
	) {
		String code = getMyCodeService.getCode(user.getId());
		GetMyCodeResponse response = new GetMyCodeResponse(code);
		return DorunResponse.success("내 친구 코드 조회가 성공적으로 처리되었습니다.", response);
	}

	@GetMapping("/api/friends/running/status")
	public DorunResponse<PaginationResponse<FriendRunningStatusResponse>> getFriendsRunningStatus(
		@CurrentUser User user,
		@ModelAttribute FriendRunningStatusRequest request
	) {
		PaginationResponse<FriendRunningStatusResponse> response = getFriendsRunningStatusService.find(
			user.getId(),
			request.page(),
			request.size()
		);
		return DorunResponse.success("친구 러닝 현황 조회가 성공적으로 처리되었습니다.", response);
	}

	@PostMapping("/api/friends/delete")
	public DorunResponse<Void> deleteFriends(
		@CurrentUser User user,
		@Valid @RequestBody DeleteFriendsRequest request
	) {
		deleteFriendsService.delete(user.getId(), request.friendIds());
		return DorunResponse.success("친구가 성공적으로 삭제되었습니다.", null);
	}

	@PostMapping("/api/friends/reaction")
	public DorunResponse<Void> cheerFriend(
		@CurrentUser User user,
		@Valid @RequestBody CheerFriendRequest request
	) {
		cheerFriendService.cheer(user.getId(), request.userId());
		return DorunResponse.success("친구 응원이 성공적으로 처리되었습니다.", null);
	}
}
