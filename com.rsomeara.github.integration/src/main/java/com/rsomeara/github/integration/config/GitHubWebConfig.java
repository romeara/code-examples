package com.rsomeara.github.integration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rsomeara.github.integration.server.GitHubWebHookRestServer;

@Configuration
public class GitHubWebConfig {

	@Bean
	public GitHubWebHookRestServer gitHubWebHookRestServer() {
		return new GitHubWebHookRestServer();
	}
}
