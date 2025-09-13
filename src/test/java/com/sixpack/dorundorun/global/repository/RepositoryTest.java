package com.sixpack.dorundorun.global.repository;

import com.sixpack.dorundorun.global.config.TestConfig;
import org.junit.jupiter.api.Disabled;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@Disabled
public abstract class RepositoryTest {
}
