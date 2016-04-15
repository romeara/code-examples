package com.rsomeara.restlet.app.resource.impl;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.restlet.resource.ServerResource;

import com.rsomeara.restlet.app.resource.HelloResource;
import com.rsomeara.restlet.app.service.IHelloService;

/**
 * Implementation of hello resource which uses spring service(s) to fulfill requests
 *
 * @author romeara
 * @since 0.1
 */
public class HelloServerResource extends ServerResource implements HelloResource {

    private IHelloService helloService;

    /**
     * Constructor which accepts an instance of a back-end service to perform operations. Detectable by spring wiring
     * routines as a valid dependency injection target
     *
     * @param helloService
     *            Instance of the back-end service to use when performing operations
     * @since 0.1
     */
    @Inject
    public HelloServerResource(@Nonnull IHelloService helloService) {
        Objects.requireNonNull(helloService);

        this.helloService = helloService;
    }

    @Override
    public String represent() {
        return helloService.getMessage();
    }

}
