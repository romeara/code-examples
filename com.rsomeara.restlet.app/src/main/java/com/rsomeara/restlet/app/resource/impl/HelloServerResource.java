package com.rsomeara.restlet.app.resource.impl;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.restlet.resource.ServerResource;

import com.rsomeara.restlet.app.resource.HelloResource;
import com.rsomeara.restlet.app.service.IHelloService;

public class HelloServerResource extends ServerResource implements HelloResource {

    private IHelloService helloService;

    public HelloServerResource() {
    }

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
