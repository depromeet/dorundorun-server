package com.sixpack.dorundorun.feature.user.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.user.dto.response.UserResponse;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

	@Override
	@GetMapping("/api/users/me")
	public DorunResponse<UserResponse> getMyInfo(@CurrentUser User currentUser) {
		UserResponse response = UserResponse.of(currentUser);
		return DorunResponse.success(response);
	}
}
