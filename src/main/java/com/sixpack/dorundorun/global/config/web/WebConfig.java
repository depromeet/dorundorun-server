package com.sixpack.dorundorun.global.config.web;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.sixpack.dorundorun.global.aop.resolver.CurrentUserArgumentResolver;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final CurrentUserArgumentResolver currentUserArgumentResolver;

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(currentUserArgumentResolver);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/api/images/**")
			.addResourceLocations("classpath:/static/images/");
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new StringToLocalDateTimeConverter());
	}

	public static class StringToLocalDateTimeConverter implements Converter<String, LocalDateTime> {
		private static final List<DateTimeFormatter> FORMATTERS = List.of(
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
			DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
		);

		@Override
		public LocalDateTime convert(String source) {
			if (source.trim().isEmpty()) {
				return null;
			}

			for (DateTimeFormatter formatter : FORMATTERS) {
				try {
					return LocalDateTime.parse(source, formatter);
				} catch (Exception ignored) {
				}
			}

			throw new IllegalArgumentException("Cannot parse LocalDateTime: " + source +
				". Supported formats: ISO formats with/without Z suffix and microseconds/milliseconds");
		}
	}

}
