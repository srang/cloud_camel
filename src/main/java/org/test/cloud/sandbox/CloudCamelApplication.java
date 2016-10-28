package org.test.cloud.sandbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan("org.test.cloud")
@SpringBootApplication
public class CloudCamelApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudCamelApplication.class, args);
	}
}
