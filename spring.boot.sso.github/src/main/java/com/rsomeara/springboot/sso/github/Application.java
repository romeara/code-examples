package com.rsomeara.springboot.sso.github;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@EnableOAuth2Client
@EnableAuthorizationServer
@EnableWebSecurity
@Order(6)
@Import({ OAuthConfiguration.class, WebSecurityConfiguration.class })
public class Application {

    @Autowired
    private OAuth2RestTemplate gitHubRestTemplate;

    @RequestMapping({ "/user", "/me" })
    public Map<String, String> user(Principal principal) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("name", principal.getName());

        Map<String, String> parameters = new HashMap<>();
        parameters.put("visibility", "public");

        String result = gitHubRestTemplate.getForObject("https://api.github.com/user/repos", String.class, parameters);

        map.put("repos", result);

        return map;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
