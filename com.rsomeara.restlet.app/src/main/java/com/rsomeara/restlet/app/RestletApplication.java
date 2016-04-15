package com.rsomeara.restlet.app;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.rsomeara.restlet.app.resource.impl.HelloServerResource;
import com.rsomeara.restlet.app.service.impl.HelloSpringConfiguration;

public class RestletApplication extends Application {

    public RestletApplication() {
    }

    @Override
    public Restlet createInboundRoot() {
        // Cannot be done in constructor - context not yet populated
        configureSpringInjection();

        // Need to create via context and set finder class manually, as at this point in setup the "application" does
        // not conceptually exist yet
        Router router = new Router(getContext());
        router.setFinderClass(getFinderClass());

        router.attach("/hello", HelloServerResource.class);

        return router;
    }

    private void configureSpringInjection() {
        getContext().getAttributes().put(SpringContextFinder.SPRING_CONTEXT_ATTRIBUTE_KEY,
                new AnnotationConfigApplicationContext(HelloSpringConfiguration.class));
        setFinderClass(SpringContextFinder.class);
    }

}
