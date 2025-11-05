package com.sixpack.dorundorun.feature.user.api;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.user.dto.request.DeviceTokenUpdateRequest;
import com.sixpack.dorundorun.feature.user.dto.request.MyProfileUpdateRequest;
import com.sixpack.dorundorun.feature.user.dto.response.MyProfileResponse;
import com.sixpack.dorundorun.feature.user.dto.response.NewProfileResponse;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "[사용자 관련]")
public interface UserApi {

	@Operation(summary = "내 프로필 상세 조회", description = "현재 로그인한 사용자의 상세 프로필 정보를 조회합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK - 프로필 조회 성공"),
		@ApiResponse(responseCode = "401", description = "UNAUTHORIZED - 유효하지 않거나 만료되었거나 누락된 토큰")
	})
	DorunResponse<MyProfileResponse> getMyProfile(
		@Parameter(hidden = true) @CurrentUser User currentUser
	);

	@Operation(summary = "내 프로필 수정",
		description = """
			현재 로그인한 사용자의 프로필 정보(닉네임, 프로필 이미지)를 수정합니다.
			
			**요청 형식:**
			- Content-Type: multipart/form-data
			- data: MyProfileUpdateRequest (nickname, imageOption)
			- profileImage: 프로필 이미지 파일 (imageOption=SET인 경우 필수)
			
			**프로필 이미지 처리 옵션:**
			- SET: 새 이미지로 교체 (profileImage 파일 필수)
			- REMOVE: 이미지 삭제
			- KEEP: 기존 이미지 유지 (또는 null일 경우 기본값)
			""")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK - 프로필 수정 성공"),
		@ApiResponse(responseCode = "400", description = "BAD_REQUEST - 올바르지 않은 닉네임 형식 (2~8자, 한글 허용)"),
		@ApiResponse(responseCode = "401", description = "UNAUTHORIZED - 유효하지 않거나 만료되었거나 누락된 토큰")
	})
	DorunResponse<NewProfileResponse> updateMyProfile(
		@Parameter(hidden = true) @CurrentUser User currentUser,

		@Parameter(
			description = "프로필 수정 데이터",
			required = true,
			schema = @Schema(
				implementation = MyProfileUpdateRequest.class,
				type = "string",
				format = "json"
			)
		)
		@RequestPart(value = "data") String dataJson,

		@Parameter(description = "프로필 이미지", required = false)
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage
	);

	@Operation(summary = "디바이스 토큰 수정", description = "FCM 디바이스 토큰을 수정합니다.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "OK - 디바이스 토큰 수정 성공"),
		@ApiResponse(responseCode = "401", description = "UNAUTHORIZED - 유효하지 않거나 만료되었거나 누락된 토큰")
	})
	DorunResponse<Void> updateDeviceToken(
		@Parameter(hidden = true) @CurrentUser User currentUser,
		@RequestBody DeviceTokenUpdateRequest request
	);
}
