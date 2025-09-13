package com.sixpack.dorundorun.global.service;


import com.sixpack.dorundorun.global.config.TestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = TestConfig.class)
public abstract class ServiceTest {

}

