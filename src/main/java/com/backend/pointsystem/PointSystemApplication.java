package com.backend.pointsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PointSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(PointSystemApplication.class, args);
	}

}
