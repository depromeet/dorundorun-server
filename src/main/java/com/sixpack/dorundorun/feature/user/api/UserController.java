package com.sixpack.dorundorun.feature.user.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.feature.user.application.FindMyProfileService;
import com.sixpack.dorundorun.feature.user.application.UpdateDeviceTokenService;
import com.sixpack.dorundorun.feature.user.application.UpdateMyProfileService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.user.dto.request.DeviceTokenUpdateRequest;
import com.sixpack.dorundorun.feature.user.dto.response.MyProfileResponse;
import com.sixpack.dorundorun.feature.user.dto.response.NewProfileResponse;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

	private final FindMyProfileService findMyProfileService;
	private final UpdateMyProfileService updateMyProfileService;
	private final UpdateDeviceTokenService updateDeviceTokenService;

	@Override
	@GetMapping("/api/users/me/profile")
	public DorunResponse<MyProfileResponse> getMyProfile(@CurrentUser User currentUser) {
		MyProfileResponse response = findMyProfileService.find(currentUser);
		return DorunResponse.success(response);
	}

	@Override
	@PatchMapping(value = "/api/users/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DorunResponse<NewProfileResponse> updateMyProfile(
		@CurrentUser User currentUser,
		@RequestPart(value = "data") String dataJson,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage
	) {
		NewProfileResponse response = updateMyProfileService.updateProfile(currentUser, dataJson, profileImage);
		return DorunResponse.success(response);
	}

	@Override
	@PatchMapping("/api/users/me/device-token")
	public DorunResponse<Void> updateDeviceToken(
		@CurrentUser User currentUser,
		@Valid @RequestBody DeviceTokenUpdateRequest request
	) {
		updateDeviceTokenService.updateDeviceToken(currentUser, request);
		return DorunResponse.success("FCM device token 업데이트가 성공적으로 진행됐습니다.");
	}
}
