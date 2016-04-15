package com.rsomeara.restlet.app;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.restlet.Component;
import org.restlet.data.Protocol;

/**
 * Simple main class which starts up the Restlet component hosting the sample application
 *
 * @author romeara
 * @since 0.1
 */
public class Main {

    @Option(name = "--port", usage = "Port to start the example servlet on", required = false)
    private int port = 8000;

    public static void main(String[] args) {
        new Main().runRestlet(args);
    }

    private void runRestlet(String args[]) {
        CmdLineParser parser = new CmdLineParser(this);

        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            System.err.println();
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }

        Component component = new RestletComponent();
        component.getServers().add(Protocol.HTTP, port);

        System.out.println("Started application on port " + port);

        try {
            component.start();
        } catch (Exception e) {
            System.err.println("Failed to start restlet");
            e.printStackTrace();
        }

        // Add a shutdown process so that when the JVM is shut down, the Restlet component also shuts down gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(new RestletShutdownProcess(component)));
    }

    /**
     * Task which will stop the component
     *
     * @author romeara
     * @since 0.1
     */
    private static final class RestletShutdownProcess implements Runnable {

        private final Component restletComponent;

        public RestletShutdownProcess(@Nonnull Component restletComponent) {
            Objects.requireNonNull(restletComponent);
            this.restletComponent = restletComponent;
        }

        @Override
        public void run() {
            try {
                restletComponent.stop();
            } catch (Exception e) {
                System.err.println("Failed to stop Registration Service cleanly");
                e.printStackTrace();
            }
        }

    }

}
