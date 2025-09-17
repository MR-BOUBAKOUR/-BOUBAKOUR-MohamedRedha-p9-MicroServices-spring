package com.MedilaboSolutions.notifications;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// allow our notifications ms to connect to the other microservices
// will replace the usage of "RestTemplate / webClient"
// --> Declarative approach (a JPA repository "like" approach")
@EnableFeignClients
@SpringBootApplication
public class NotificationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationsApplication.class, args);
	}

}
