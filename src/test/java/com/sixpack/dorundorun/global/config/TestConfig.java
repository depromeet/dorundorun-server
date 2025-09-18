package com.sixpack.dorundorun.global.config;

import com.sixpack.dorundorun.global.config.testcontainers.MySQLTestConfig;
import com.sixpack.dorundorun.global.config.testcontainers.RedisTestConfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@Import({
	MySQLTestConfig.class,
	RedisTestConfig.class
})
@TestConfiguration
public class TestConfig {
}

