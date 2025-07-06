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
						.path("/login")
						.filters(f -> f
						)
						.uri("forward:/login")
				)
				.route(p -> p
						.path("/refresh")
						.filters(f -> f
						)
						.uri("forward:/refresh")
				)
				.route(p -> p
						.path("/logout")
						.filters(f -> f
						)
						.uri("forward:/logout")
				)
				.route(p -> p
						.path("/v1/assessments/**")
						.filters( f -> f
								.rewritePath("/v1/assessments/(?<segment>.*)","/assessments/${segment}")
						)
						.uri("lb://assessments")
				)
				.route(p -> p
						.path("/v1/notes")
						.filters(f -> f
								.rewritePath("/v1/notes", "/notes")
						)
						.uri("lb://notes")
				)
				.route(p -> p
						.path("/v1/notes/**")
						.filters( f -> f
								.rewritePath("/v1/notes/(?<segment>.*)","/notes/${segment}")
						)
						.uri("lb://notes")
				)
				.route(p -> p
						.path("/v1/patients")
						.filters(f -> f
								.rewritePath("/v1/patients", "/patients")
						)
						.uri("lb://patients")
				)
				.route(p -> p
						.path("/v1/patients/**")
						.filters(f -> f
								.rewritePath("/v1/patients/(?<segment>.*)", "/patients/${segment}")
						)
						.uri("lb://patients")
				)
				.build();
	}

}
