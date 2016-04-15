package com.rsomeara.restlet.app.resource;

import org.restlet.resource.Get;

public interface HelloResource {

    @Get
    String represent();
}
