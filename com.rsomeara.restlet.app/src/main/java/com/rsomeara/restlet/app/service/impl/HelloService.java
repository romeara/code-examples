package com.rsomeara.restlet.app.service.impl;

import org.springframework.stereotype.Service;

import com.rsomeara.restlet.app.service.IHelloService;

@Service
public class HelloService implements IHelloService {

    @Override
    public String getMessage() {
        return "Hello from the service layer";
    }

}
