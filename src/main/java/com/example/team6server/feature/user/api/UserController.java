package com.example.team6server.feature.user.api;

import com.example.team6server.feature.user.domain.User;
import com.example.team6server.feature.user.dto.response.UserResponse;
import com.example.team6server.global.aop.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

	@Override
	@GetMapping("/api/users/me")
	public ResponseEntity<UserResponse> getMyInfo(@CurrentUser User currentUser) {
		UserResponse response = UserResponse.of(currentUser);
		return ResponseEntity.ok(response);
	}
}