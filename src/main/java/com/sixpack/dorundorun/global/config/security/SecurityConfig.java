package com.sixpack.dorundorun.global.config.security;

import static org.springframework.security.config.Customizer.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sixpack.dorundorun.feature.auth.exception.AuthErrorCode;
import com.sixpack.dorundorun.global.response.DorunResponse;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ObjectMapper objectMapper;

	@Bean
	@Order(1)
	public SecurityFilterChain swaggerFilterChain(HttpSecurity http) throws Exception {
		defaultFilterChain(http);

		http.securityMatcher("/swagger-ui/**", "/v3/api-docs/**");
		http.httpBasic(withDefaults());
		http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());

		return http.build();
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		defaultFilterChain(http);

		http
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(exception ->
				exception
					.authenticationEntryPoint(
						(request, response, authException) -> {
							response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
							response.setContentType("application/json;charset=UTF-8");

							DorunResponse<Void> errorResponse =
								DorunResponse.error(AuthErrorCode.INVALID_TOKEN);

							response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
						})
			)
			.authorizeHttpRequests(authorize ->
				authorize
					.requestMatchers("/public/**").permitAll()
					.requestMatchers("/api/health").permitAll()
					.requestMatchers("/actuator/**").permitAll()
					.requestMatchers("/api/auth/sms/**").permitAll()
					.requestMatchers("/api/auth/signup").permitAll()
					.requestMatchers("/api/auth/refresh").permitAll()
					.requestMatchers("/api/images/**").permitAll()
					.anyRequest().authenticated());

		return http.build();
	}

	private void defaultFilterChain(HttpSecurity http) throws Exception {
		http
			.httpBasic(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.cors(withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			);
	}

	// TODO: 임시 cors 설정
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.addAllowedOriginPattern("*");
		configuration.addAllowedHeader("*");
		configuration.addAllowedMethod("*");
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

