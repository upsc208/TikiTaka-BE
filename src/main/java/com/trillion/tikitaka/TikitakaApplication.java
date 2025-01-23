package com.trillion.tikitaka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TikitakaApplication {

	public static void main(String[] args) {
		SpringApplication.run(TikitakaApplication.class, args);
	}

}
