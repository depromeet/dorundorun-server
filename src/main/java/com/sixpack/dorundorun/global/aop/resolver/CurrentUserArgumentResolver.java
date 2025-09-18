package com.sixpack.dorundorun.global.aop.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

	public static final String USER_ID_HEADER = "X-User-Id";

	private final FindUserByIdService findUserByIdService;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CurrentUser.class)
			&& parameter.getParameterType().equals(User.class);
	}

	@Override
	public Object resolveArgument(
		MethodParameter parameter,
		ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest,
		WebDataBinderFactory binderFactory
	) throws Exception {

		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);

		// 헤더에서 userId 가져오기
		// TODO: JWT 토큰에서 파싱하거나 세션에서 가져올 수 있음
		String userIdHeader = request != null ? request.getHeader(USER_ID_HEADER) : null;

		if (!StringUtils.hasText(userIdHeader)) {
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
