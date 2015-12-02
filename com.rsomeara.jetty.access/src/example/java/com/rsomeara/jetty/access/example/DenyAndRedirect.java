package com.rsomeara.jetty.access.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rsomeara.jetty.access.handler.ConfigurableIPAccessHandler;
import com.rsomeara.jetty.access.handler.IAccessPolicy;

/**
 * Simple access handler example which starts two local servers, one which redirects local requests to the other
 *
 * @author romeara
 */
public class DenyAndRedirect {

    /** Logger reference to output information to the application log files */
    private static final Logger logger = LoggerFactory.getLogger(DenyAndRedirect.class);

    @Option(name = "--target-port", usage = "The port of the target server. Default 9999")
    private int targetPort = 9999;

    @Option(name = "--redirect-port", usage = "The port of the server which requests will be redirected to. Defaults to a an open port")
    private int redirectPort = -1;

    public static void main(String[] args) throws Exception {
        new DenyAndRedirect().doMain(args);
    }

    public void doMain(String[] args) throws Exception {
        CmdLineParser parser = new CmdLineParser(this);
        Server targetServer = null;
        Server redirectDestinationServer = null;

        try {
            // Parse the arguments.
            parser.parseArgument(args);

            if (redirectPort == -1) {
                redirectPort = findRandomPort();
            }

            // Setup two servers - one provide a simple message when a GET is performed, another to redirect requests
            // from the local machine to the first server
            redirectDestinationServer = configureRedirectDestinationServer(redirectPort, "allow");
            redirectDestinationServer.start();
            String allowServerAddress = getServerAddress(redirectDestinationServer) + "/allow";

            targetServer = configureTargetServer(targetPort, allowServerAddress);
            targetServer.start();
            String denyServerAddress = getServerAddress(targetServer) + "/deny";

            // Make a request to the server that is setup to redirect requests from the local machine. This should
            // redirect to the GET server and read a simple message
            HttpURLConnection.setFollowRedirects(true);

            URL targetUrl = new URL(denyServerAddress);
            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();

            connection.setRequestMethod("GET");
            connection.setDoInput(true);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line = reader.readLine();

                while (line != null) {
                    logger.info(line);

                    line = reader.readLine();
                }
            }
        } catch (CmdLineException e) {
            e.printStackTrace();
        } finally {
            // Shut down both servers
            try {
                if (targetServer != null) {
                    targetServer.stop();
                }
            } finally {
                if (redirectDestinationServer != null) {
                    redirectDestinationServer.stop();
                }
            }
        }
    }

    /**
     * @param server
     *            Server instance to read address information from
     * @return The HTTP address of the server, including port information
     */
    private String getServerAddress(Server server) {
        ServerConnector connector = (ServerConnector) server.getConnectors()[0];
        return "http://localhost:" + connector.getPort();
    }

    /**
     * Configures a server which has restrictions on access from the local machine. Will redirect requests from the
     * local machine to a specified address
     *
     * @param port
     *            The port to put the server on
     * @param redirectAddress
     *            The address to redirect client calls to if they come from the local machine
     * @return The server instance configured to redirect local requests. Server should be started by clients
     */
    private Server configureTargetServer(int port, String redirectAddress) {
        Server denyServer = new Server();

        ServerConnector connector = new ServerConnector(denyServer);
        connector.setPort(port);

        ServletHandler handler = new ServletHandler();
        ServletHolder denyHolder = new ServletHolder(new HttpServlet() {
        });
        denyHolder.setAsyncSupported(true);
        handler.addServletWithMapping(denyHolder, "/deny");

        ConfigurableIPAccessHandler denyHandler = new ConfigurableIPAccessHandler(new RedirectingAccessPolicy(redirectAddress));
        denyHandler.addBlack("127.0.0.1");
        denyHandler.addBlack("127.0.1.1");
        denyHandler.setHandler(handler);

        denyServer.setHandler(denyHandler);

        denyServer.setConnectors(new Connector[] { connector });

        return denyServer;
    }

    /**
     * Configures a server which is open to access, and will used as the redirect target for this example
     *
     * @param port
     *            The port to put the server on
     * @param servletPath
     *            The sub-path to host a simple GET servlet on
     * @return The server instance configured to host a GET end-point. Server should be started by clients
     */
    private Server configureRedirectDestinationServer(int port, String servletPath) {
        Server redirectDestinationServer = new Server();

        ServerConnector connector = new ServerConnector(redirectDestinationServer);
        connector.setPort(port);

        ServletHandler servletHandler = new ServletHandler();
        ServletHolder holder = new ServletHolder(new GetServlet());
        holder.setAsyncSupported(true);
        servletHandler.addServletWithMapping(holder, "/" + servletPath);

        redirectDestinationServer.setHandler(servletHandler);

        redirectDestinationServer.setConnectors(new Connector[] { connector });

        return redirectDestinationServer;
    }

    /**
     * @return A port on the local machine which is currently not used
     * @throws IOException
     *             If an I/O occurs when attempting to open and verify an unused port
     */
    private int findRandomPort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    /**
     * Access policy which will redirect denied requests to a specified address. A client might use something like this
     * to force callers to go through an authenticated entry point
     *
     * @author romeara
     */
    private static final class RedirectingAccessPolicy implements IAccessPolicy {

        private final String redirectAddress;

        /**
         * @param redirectAddress
         *            The web address to redirect requests to
         */
        public RedirectingAccessPolicy(String redirectAddress) {
            this.redirectAddress = redirectAddress;
        }

        @Override
        public void handleDenied(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            // Redirect to another location if the request did not come from a desired source
            response.setStatus(HttpServletResponse.SC_FOUND);
            response.sendRedirect(redirectAddress);
        }

        @Override
        public void handleAllowed(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            // Do nothing
        }
    }

    /**
     * HTTP servlet that exposes a simple GET end-point for a client to access
     *
     * @author romeara
     */
    private static final class GetServlet extends HttpServlet {

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello World</h1>");
            response.getWriter().flush();
        }

    }
}
