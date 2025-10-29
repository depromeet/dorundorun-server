package com.sixpack.dorundorun.global.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.sixpack.dorundorun.global.config.testcontainers.MySQLTestConfig;
import com.sixpack.dorundorun.global.config.testcontainers.RedisTestConfig;
import com.sixpack.dorundorun.infra.firebase.FcmService;

@Import({
	MySQLTestConfig.class,
	RedisTestConfig.class
})
@TestConfiguration
public class TestConfig {

	// 테스트 환경에서 사용할 Mock FcmService
	@Bean
	public FcmService fcmService() {
		FcmService mockService = Mockito.mock(FcmService.class);
		// FCM이 disabled 상태를 시뮬레이션
		Mockito.when(mockService.isEnabled()).thenReturn(false);
		return mockService;
	}
}

