package com.example.team6server.global.config;

import com.example.team6server.global.config.testcontainers.MySQLTestConfig;
import com.example.team6server.global.config.testcontainers.RedisTestConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@Import({
		MySQLTestConfig.class,
		RedisTestConfig.class
})
@TestConfiguration
public class TestConfig {
}

