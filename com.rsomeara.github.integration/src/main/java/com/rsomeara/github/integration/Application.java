package com.rsomeara.github.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.rsomeara.github.integration.config.GitHubWebConfig;

@SpringBootApplication
@Import({ GitHubWebConfig.class })
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}