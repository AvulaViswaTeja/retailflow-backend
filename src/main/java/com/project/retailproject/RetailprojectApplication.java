package com.project.retailproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(
		info = @Info(
				title = "RetailFlow API",
				version = "1.0",
				description = "API documentation for managing retailflow"
		)
)
public class RetailprojectApplication {

	public static void main(String[] args) {
		SpringApplication.run(RetailprojectApplication.class, args);
	}

}
