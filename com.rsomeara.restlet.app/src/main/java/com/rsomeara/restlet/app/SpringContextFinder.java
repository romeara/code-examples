package com.rsomeara.restlet.app;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * Restlet Finder which utilizes spring's dependency injection capabilities to provide resource implementations with
 * access to services and other resources present in the application
 *
 * <p>
 * The spring application context is retrieved via the Restlet context because custom Finder constructors are not easily
 * supported, and Finders may be instantiated per target resource call
 * </p>
 *
 * @author romeara
 * @since 0.1
 */
public class SpringContextFinder extends Finder {

    /** Key used to store a spring application context within the Restlet context of an application */
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
            // Try to lookup an existing bean before creating a new one
            try {
                resource = springContext.getBean(targetClass);
            } catch (NoSuchBeanDefinitionException e) {
                resource = springContext.getAutowireCapableBeanFactory().createBean(targetClass);
            }
        } else {
            // If spring was not configured, fall back to the standard way of finding server resource definitions
            resource = super.create(targetClass, request, response);
        }

        return resource;
    }

    /**
     * Looks for a spring application context within the Restlet context known to this Finder
     *
     * @return A spring application context, if one was found. Otherwise, null
     */
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
