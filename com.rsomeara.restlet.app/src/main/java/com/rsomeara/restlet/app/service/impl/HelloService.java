package com.rsomeara.restlet.app.service.impl;

import org.springframework.stereotype.Service;

import com.rsomeara.restlet.app.service.IHelloService;

/**
 * Implementation of {@link IHelloService}
 *
 * <p>
 * Clients should NOT refer to this class directly. Use dependency injection to obtain the application's configured
 * instance of {@link IHelloService} instead
 * </p>
 *
 * @author romeara
 * @since 0.1
 */
@Service
public class HelloService implements IHelloService {

    @Override
    public String getMessage() {
        return "Hello from the service layer!";
    }

}
