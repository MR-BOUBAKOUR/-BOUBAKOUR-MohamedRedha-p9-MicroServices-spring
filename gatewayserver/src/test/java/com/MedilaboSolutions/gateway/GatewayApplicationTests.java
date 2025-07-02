package com.MedilaboSolutions.gateway;

import com.MedilaboSolutions.gateway.config.AbstractPostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"eureka.client.enabled=false",
		"eureka.client.register-with-eureka=false",
		"eureka.client.fetch-registry=false"
})
class GatewayApplicationTests extends AbstractPostgresContainerTest {

	@Test
	void contextLoads() {
	}

}
