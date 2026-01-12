package com.exchangeratechallenge.exchangeRateAPI;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "exchangeAPI=dummy")
class ExchangeRateApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
