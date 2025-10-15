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

	public static final String AUTHORIZATION_HEADER = "Authorization";

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

		String authHeader = request != null ? request.getHeader(AUTHORIZATION_HEADER) : null;

		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
			throw new IllegalArgumentException("Authorization header with Bearer token is required");
		}

		try {
			String userIdToken = authHeader.substring(7);
			Long userId = Long.parseLong(userIdToken);
			return findUserByIdService.find(userId);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid User ID format in Bearer token", e);
		}
	}
}
