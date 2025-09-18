package com.sixpack.dorundorun.global.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.sixpack.dorundorun.global.config.TestConfig;

@ActiveProfiles("test")
@SpringBootTest(classes = TestConfig.class)
public abstract class ServiceTest {
}
