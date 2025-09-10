package com.example.team6server.global.config.security;

import jakarta.servlet.http.HttpServletResponse;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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

		http.exceptionHandling(exception -> exception
						.authenticationEntryPoint((request, response, authException) -> {
							response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
						})
				) // TODO 예외 응답 형식 통일
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers("/public/**").permitAll()
						.requestMatchers("/api/health").permitAll()
						.requestMatchers("/api/**").permitAll()
						.anyRequest().authenticated());

		return http.build();
	}

	private void defaultFilterChain(HttpSecurity http) throws Exception {
		http.httpBasic(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable).cors(withDefaults()).csrf(AbstractHttpConfigurer::disable).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
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

