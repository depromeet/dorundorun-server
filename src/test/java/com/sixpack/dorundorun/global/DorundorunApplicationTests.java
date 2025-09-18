package com.sixpack.dorundorun.global;

import com.sixpack.dorundorun.global.config.TestConfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = TestConfig.class)
class DorundorunApplicationTests {

	@Test
	void contextLoads() {
	}

}
