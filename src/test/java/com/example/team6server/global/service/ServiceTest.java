package com.example.team6server.global.service;


import com.example.team6server.global.config.TestConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = TestConfig.class)
public abstract class ServiceTest {

}

