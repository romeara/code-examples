package com.rsomeara.restlet.app.service.impl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rsomeara.restlet.app.service.IHelloService;

/**
 * Spring configuration class which provides object instances which can be injected as dependencies throughout the
 * application
 *
 * @author romeara
 * @since 0.1
 */
@Configuration
public class HelloSpringConfiguration {

    /**
     * @return Hello service implementation to fulfill dependencies on IHelloService in the application
     * @since 0.1
     */
    @Bean
    public IHelloService helloService() {
        return new HelloService();
    }
}
