package com.rsomeara.jetty.access.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.EndPoint;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.IPAccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jetty request handler which restricts access to protected end-points based on the IP address making the request.
 * Allows configurable handling of denied and allowed requests. If no custom handling is specified, then request are
 * handled identically to {@link IPAccessHandler}
 *
 * @author romeara
 * @since 0.1
 */
public class ConfigurableIPAccessHandler extends IPAccessHandler {

    /** Logger reference to output information to the application log files */
    private static final Logger logger = LoggerFactory.getLogger(ConfigurableIPAccessHandler.class);

    private final IAccessPolicy accessPolicy;

    /**
     * Creates new handler object with the default access policy
     *
     * @since 0.1
     */
    public ConfigurableIPAccessHandler() {
        this(new DefaultAccessPolicy());
    }

    /**
     * Creates new handler object with the default access policy
     *
     * @param accessPolicy
     *            An policy which determines how allowed and denied requests will be handled. Must not be null
     * @since 0.1
     */
    public ConfigurableIPAccessHandler(@Nonnull IAccessPolicy accessPolicy) {
        super();
        Objects.requireNonNull(accessPolicy, "Access policy is required");

        this.accessPolicy = accessPolicy;
    }

    /**
     * Creates new handler object with the default access policy and initializes white-list and black-list
     *
     * @param whiteList
     *            Array of white-list entries
     * @param blackList
     *            Array of black-list entries
     * @since 0.1
     */
    public ConfigurableIPAccessHandler(@Nullable String[] whiteList, @Nullable String[] blackList) {
        this(whiteList, blackList, new DefaultAccessPolicy());
    }

    /**
     * Creates new handler object with the a custom access policy and initializes white-list and black-list
     *
     * @param whiteList
     *            Array of white-list entries
     * @param blackList
     *            Array of black-list entries
     * @param accessPolicy
     *            An policy which determines how allowed and denied requests will be handled. Must not be null
     * @since 0.1
     */
    public ConfigurableIPAccessHandler(@Nullable String[] white, @Nullable String[] black, @Nonnull IAccessPolicy accessPolicy) {
        super(white, black);
        Objects.requireNonNull(accessPolicy, "Access policy is required");

        this.accessPolicy = accessPolicy;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // Get the real remote IP (not the one set by the forwarded headers (which may be forged))
        Optional<InetSocketAddress> address = getAddress(baseRequest);

        if (address.isPresent() && !isAddrUriAllowed(address.get().getHostString(), baseRequest.getPathInfo())) {
            logger.trace("Access denied to request from {} to {} based on IP access configuration", address.get().getHostString(), target);

            accessPolicy.handleDenied(target, baseRequest, request, response);
        } else {
            if (!address.isPresent()) {
                logger.warn("Could not read address information from incoming request - IP access filtering not applied");
            } else {
                logger.trace("Access allowed for request from {} to {} based on IP access configuration", address.get().getHostString(), target);
            }

            accessPolicy.handleAllowed(target, baseRequest, request, response);
            getHandler().handle(target, baseRequest, request, response);
        }
    }

    /**
     * Reads the socket address a request came from, if possible
     *
     * @param baseRequest
     *            The base request made
     * @return The address the request was made from, or an absent optional if the address could not be read
     */
    private Optional<InetSocketAddress> getAddress(Request baseRequest) {
        InetSocketAddress address = null;

        HttpChannel channel = baseRequest.getHttpChannel();

        if (channel != null) {
            EndPoint endp = channel.getEndPoint();

            if (endp != null) {
                address = endp.getRemoteAddress();
            }
        }

        if (logger.isTraceEnabled() && address == null) {
            logger.trace("Unabled to read address (Available Data: [Channel: {}, Endpoint:{}])",
                    (channel != null), (channel != null && channel.getEndPoint() != null));
        }

        return Optional.ofNullable(address);
    }

    /**
     * Access policy which handles requests being allowed/denied in an identical way to {@link IPAccessHandler}. When
     * requests are denied, sends a '403 - FORBIDDEN' response and sets the base request to "handled". When requests are
     * allowed, performs no special actions
     *
     * @author romeara
     */
    private static final class DefaultAccessPolicy implements IAccessPolicy {

        @Override
        public void handleDenied(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.sendError(HttpStatus.FORBIDDEN_403);
            baseRequest.setHandled(true);
        }

        @Override
        public void handleAllowed(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            // Do nothing special - original behavior of IPAccessHandler
        }

    }
}
