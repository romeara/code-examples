package com.rsomeara.restlet.app;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.springframework.context.ApplicationContext;

public class SpringContextFinder extends Finder {

    public static final String SPRING_CONTEXT_ATTRIBUTE_KEY = "org.rsomeara.restlet.app.spring.context";

    public SpringContextFinder() {
        super();
    }

    public SpringContextFinder(Context context) {
        super(context);
    }

    public SpringContextFinder(Context context, Class<? extends ServerResource> targetClass) {
        super(context, targetClass);
    }

    @Override
    public ServerResource create(Class<? extends ServerResource> targetClass, Request request, Response response) {
        ApplicationContext springContext = getSpringContext();

        ServerResource resource = null;

        // Allowing spring to do this as a bean operation allows use of more injection features, such as constructor
        // injection
        if (springContext != null) {
            resource = getSpringContext().getAutowireCapableBeanFactory().createBean(targetClass);
        } else {
            resource = super.create(targetClass, request, response);
        }

        return resource;
    }

    private ApplicationContext getSpringContext() {
        ApplicationContext springContext = null;
        Context context = getContext();

        if (context != null) {
            Object attributeValue = context.getAttributes().get(SPRING_CONTEXT_ATTRIBUTE_KEY);

            if (attributeValue instanceof ApplicationContext) {
                springContext = (ApplicationContext) attributeValue;
            }
        }

        return springContext;
    }

}
