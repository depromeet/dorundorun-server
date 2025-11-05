package com.sixpack.dorundorun.feature.friend.api;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;

import com.sixpack.dorundorun.feature.friend.dto.request.AddFriendRequest;
import com.sixpack.dorundorun.feature.friend.dto.request.CheerFriendRequest;
import com.sixpack.dorundorun.feature.friend.dto.request.DeleteFriendsRequest;
import com.sixpack.dorundorun.feature.friend.dto.request.FriendRunningStatusRequest;
import com.sixpack.dorundorun.feature.friend.dto.response.AddFriendResponse;
import com.sixpack.dorundorun.feature.friend.dto.response.DeleteFriendsResponse;
import com.sixpack.dorundorun.feature.friend.dto.response.FriendRunningStatusResponse;
import com.sixpack.dorundorun.feature.friend.dto.response.GetMyCodeResponse;
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

@Tag(name = "[친구 관련]")
public interface FriendApi {

	@Operation(summary = "친구 코드로 친구 추가", description = "친구 코드를 입력해서 친구를 추가합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "친구 추가 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "해당 코드의 유저를 찾을 수 없습니다.")
	})
	DorunResponse<AddFriendResponse> addFriend(
		@Parameter(hidden = true) @CurrentUser User user,
		@Valid @RequestBody AddFriendRequest request
	);

	@Operation(summary = "내 친구 코드 조회", description = "친구에게 공유할 내 친구 코드를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "코드 조회 성공")
	})
	DorunResponse<GetMyCodeResponse> getMyCode(
		@Parameter(hidden = true) @CurrentUser User user
	);

	@Operation(summary = "친구 러닝 현황 조회", description = "유저와 유저의 친구들의 러닝 현황을 조회합니다. 최상단에 본인, 나머지는 친구의 최신 러닝 시간 순으로 정렬됩니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "친구 러닝 현황 조회 성공")
	})
	DorunResponse<PaginationResponse<FriendRunningStatusResponse>> getFriendsRunningStatus(
		@Parameter(hidden = true) @CurrentUser User user,
		@ParameterObject @ModelAttribute FriendRunningStatusRequest request
	);

	@Operation(summary = "친구 삭제", description = "선택한 친구들을 삭제합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "친구 삭제 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청")
	})
	DorunResponse<DeleteFriendsResponse> deleteFriends(
		@Parameter(hidden = true) @CurrentUser User user,
		@Valid @RequestBody DeleteFriendsRequest request
	);

	@Operation(summary = "친구 응원하기", description = "최신 러닝 기록이 없는 친구를 응원합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "친구 응원 성공"),
		@ApiResponse(responseCode = "400", description = "잘못된 요청"),
		@ApiResponse(responseCode = "404", description = "해당 유저를 찾을 수 없습니다.")
	})
	DorunResponse<Void> cheerFriend(
		@Parameter(hidden = true) @CurrentUser User user,
		@Valid @RequestBody CheerFriendRequest request
	);
}
