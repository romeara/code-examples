package com.rsomeara.restlet.app;

import org.restlet.Application;
import org.restlet.Component;

/**
 * Restlet component running the system. Components are mainly responsible for resources across applications, such as
 * network connectors and virtual hosts
 *
 * @author romeara
 * @since 0.1
 */
public class RestletComponent extends Component {

    /**
     * Creates a new component instance, and configures the sample application to run
     * 
     * @since 0.1
     */
    public RestletComponent() {
        super();

        Application application = new RestletApplication();
        getDefaultHost().attachDefault(application);
    }

}
