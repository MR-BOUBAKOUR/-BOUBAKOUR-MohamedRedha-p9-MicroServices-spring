package com.MedilaboSolutions.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;

@SpringBootApplication
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Bean
	public RouteLocator customRoutes(RouteLocatorBuilder builder) {
		return builder.routes()
				.route(p -> p
						.path("/v1/assessments/**")
						.filters( f -> f
								.rewritePath("/v1/assessments/(?<segment>.*)","/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
						.uri("lb://ASSESSMENTS")
				)
				.route(p -> p
						.path("/v1/notes/**")
						.filters( f -> f
								.rewritePath("/v1/notes/(?<segment>.*)","/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
						.uri("lb://NOTES")
				)
				.route(p -> p
						.path("/v1/patients/**")
						.filters( f -> f
								.rewritePath("/v1/patients/(?<segment>.*)","/${segment}")
								.addResponseHeader("X-Response-Time", LocalDateTime.now().toString()))
						.uri("lb://PATIENTS")
				)
				.build();
	}

}
