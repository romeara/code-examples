package com.rsomeara.restlet.app.service.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rsomeara.restlet.app.service.IHelloService;

@Configuration
public class HelloSpringConfiguration {

    @Bean
    public IHelloService helloService() {
        return new HelloService();
    }
}
