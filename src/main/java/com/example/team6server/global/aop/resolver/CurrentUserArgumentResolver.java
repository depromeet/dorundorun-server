package com.example.team6server.global.aop.resolver;

import com.example.team6server.feature.user.application.FindUserByIdService;
import com.example.team6server.feature.user.domain.User;
import com.example.team6server.global.aop.annotation.CurrentUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

	private final FindUserByIdService findUserByIdService;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CurrentUser.class)
				&& parameter.getParameterType().equals(User.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter,
								  ModelAndViewContainer mavContainer,
								  NativeWebRequest webRequest,
								  WebDataBinderFactory binderFactory) throws Exception {

		HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

		// 헤더에서 userId 가져오기
		// TODO: JWT 토큰에서 파싱하거나 세션에서 가져올 수 있음
		String userIdHeader = request.getHeader("X-User-Id");

		if (userIdHeader == null) {
			throw new IllegalArgumentException("User ID header is required");
		}

		try {
			Long userId = Long.parseLong(userIdHeader);
			return findUserByIdService.find(userId);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid User ID format", e);
		}
	}
}
