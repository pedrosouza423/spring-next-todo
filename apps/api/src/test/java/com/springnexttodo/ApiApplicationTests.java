package com.springnexttodo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "app.jwt.secret=test-secret-for-context-load-test-only-32bytes")
class ApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
