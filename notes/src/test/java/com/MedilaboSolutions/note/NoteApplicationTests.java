package com.MedilaboSolutions.note;

import com.MedilaboSolutions.note.config.AbstractMongoContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"eureka.client.enabled=false",
		"eureka.client.register-with-eureka=false",
		"eureka.client.fetch-registry=false"
})
class NoteApplicationTests extends AbstractMongoContainerTest {

	@Test
	void contextLoads() {
	}
}
