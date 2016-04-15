package com.rsomeara.restlet.app.resource;

import org.restlet.resource.Get;

/**
 * Simple resource which demonstrates a GET operation definition in Restlet
 *
 * @author romeara
 * @since 0.1
 */
public interface HelloResource {

    /**
     * @return A simple greeting
     * @since 0.1
     */
    @Get
    String represent();
}
