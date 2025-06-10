package com.MedilaboSolutions.assessment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// allow our assessment ms to connect to the other microservices
// will replace the usage of "RestTemplate / webClient"
// --> Declarative approach (a JPA repository "like" approach")
@EnableFeignClients
@SpringBootApplication
public class AssessmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssessmentApplication.class, args);
	}

}
