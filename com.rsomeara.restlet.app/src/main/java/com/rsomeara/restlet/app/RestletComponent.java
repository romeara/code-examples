package com.rsomeara.restlet.app;

import org.restlet.Application;
import org.restlet.Component;

public class RestletComponent extends Component {

    public RestletComponent() {
        super();

        Application app = new RestletApplication();
        getDefaultHost().attachDefault(app);
    }
}
