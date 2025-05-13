package com.bookNDrive.payment_service;

import com.bookNDrive.payment_service.configuration.MoneticoProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(MoneticoProperties.class)
class PaymentServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
