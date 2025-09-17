package com.sixpack.dorundorun.feature.user.api;

import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.feature.user.dto.response.UserResponse;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.response.DorunResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    @Override
    @GetMapping("/api/users/me")
    public DorunResponse<UserResponse> getMyInfo(@CurrentUser User currentUser) {
        UserResponse response = UserResponse.of(currentUser);
        return DorunResponse.success(response);
    }

    // (미완) 온보딩 정보 저장 API
    @PostMapping("/api/users/{userId}/onboarding")
    public DorunResponse<Void> saveOnboardingInfo(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") String userIdHeader
            // TODO: OnboardingRequest DTO 추가 예정
            // @Valid @RequestBody OnboardingRequest request
    ) {
        // TODO: 온보딩 정보 저장 로직 구현
        return DorunResponse.success();
    }

    // (미완) 유저 Goal 저장 API
    @PostMapping("/api/users/{userId}/goals")
    public DorunResponse<Void> createUserGoal(
            @PathVariable Long userId,
            @RequestHeader("X-User-Id") String userIdHeader
            // TODO: CreateGoalRequest DTO 추가 예정
            // @Valid @RequestBody CreateGoalRequest request
    ) {
        // TODO: 유저 Goal 저장 로직 구현
        return DorunResponse.created(null);
    }
}
