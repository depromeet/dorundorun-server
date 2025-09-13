package com.sixpack.dorundorun.feature.user.api;

import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.user.dto.response.UserResponse;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "[2. 사용자 관련]")
public interface UserApi {

	@Operation(
			summary = "내 정보 조회",
			description = "현재 로그인한 사용자의 정보를 조회합니다.",
			parameters = {
					@Parameter(
							name = "X-User-Id",
							description = "사용자 ID",
							required = true,
							in = ParameterIn.HEADER,
							example = "1"
					)
			}
	)
	@ApiResponse(responseCode = "200", description = "조회 성공")
	ResponseEntity<UserResponse> getMyInfo(
			@Parameter(hidden = true) @CurrentUser User currentUser
	);
}
