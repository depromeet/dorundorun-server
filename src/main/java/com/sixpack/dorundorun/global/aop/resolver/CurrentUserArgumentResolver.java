package com.sixpack.dorundorun.global.aop.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.sixpack.dorundorun.feature.auth.exception.AuthErrorCode;
import com.sixpack.dorundorun.feature.user.application.FindUserByIdService;
import com.sixpack.dorundorun.feature.user.domain.User;
import com.sixpack.dorundorun.global.aop.annotation.CurrentUser;
import com.sixpack.dorundorun.global.config.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
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
	) {

		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		String authHeader = request != null ? request.getHeader(AUTHORIZATION_HEADER) : null;

		if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(BEARER_PREFIX)) {
			throw AuthErrorCode.MISSING_TOKEN.format();
		}

		String token = extractToken(authHeader);

		if (!jwtTokenProvider.validate(token)) {
			throw AuthErrorCode.INVALID_TOKEN.format();
		}

		Long userId = jwtTokenProvider.getUserId(token);
		return findUserByIdService.find(userId);
	}

	private String extractToken(String authHeader) {
		return authHeader.substring(BEARER_PREFIX.length());
	}
}
