package com.sixpack.dorundorun.feature.user.api;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.user.dto.response.MeProfileResponse;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

	@Override
	@GetMapping("/api/users/me/profile")
	public DorunResponse<MeProfileResponse> getMeProfile(@CurrentUser User currentUser) {
		MeProfileResponse response = MeProfileResponse.of(currentUser);
		return DorunResponse.success(response);
	}

	@Override
	@PatchMapping(value = "/api/users/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DorunResponse<Void> updateMeProfile(
		@CurrentUser User currentUser,
		@RequestPart(value = "data") String dataJson,
		@RequestPart(value = "profileImage", required = false) MultipartFile profileImage
	) {
		return DorunResponse.success("프로필 수정에 성공하였습니다");
	}
}
