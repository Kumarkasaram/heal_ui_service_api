package com.heal.dashboard.service.main;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@SpringBootTest
@Configuration
@PropertySource(value="classpath:conf.properties")
public class HealUiServiceMainTest {

	@Test
	void contextLoads() {
	}
}