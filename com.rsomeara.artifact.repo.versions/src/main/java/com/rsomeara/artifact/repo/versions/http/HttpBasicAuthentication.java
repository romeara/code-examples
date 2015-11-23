package com.rsomeara.artifact.repo.versions.http;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.google.common.collect.Lists;
import com.google.common.net.HttpHeaders;
import com.rsomeara.artifact.repo.versions.exception.AuthenticationException;

/**
 * Handles addition of HTTP basic authentication information to a connection
 *
 * @author romeara
 */
public class HttpBasicAuthentication {

    /** Constant which prefixes the HTTP basic authentication value */
    private static final String BASIC_AUTH = "Basic ";

    private final CallbackHandler callbackHandler;

    private final NameCallback nameCallback;

    private final PasswordCallback passwordCallback;

    /**
     * @param callbackHandler
     *            Handler which can populate username and password information when a request is made
     */
    public HttpBasicAuthentication(CallbackHandler callbackHandler) {
        this(callbackHandler, new NameCallback("Username: "), new PasswordCallback("Password: ", false));
    }

    /**
     * @param callbackHandler
     *            Handler which can populate username and password information when a request is made
     * @param nameCallback
     *            Callback which reads username information. May be pre-populated by clients
     * @param passwordCallback
     *            Callback which reads password information. May be pre-populated
     */
    public HttpBasicAuthentication(CallbackHandler callbackHandler, NameCallback nameCallback, PasswordCallback passwordCallback) {
        Objects.requireNonNull(callbackHandler);
        Objects.requireNonNull(nameCallback);
        Objects.requireNonNull(passwordCallback);

        this.callbackHandler = callbackHandler;
        this.nameCallback = nameCallback;
        this.passwordCallback = passwordCallback;
    }

    /**
     * Applies HTTP basic authentication information to the connection
     *
     * @param connection
     *            The connection to apply authentication information to
     * @return The provided connection with authentication information
     * @throws AuthenticationException
     *             If there is an issue obtaining authentication information. (Required callbacks not supported by
     *             handler, IO issue with handler, any authentication information is not filled in by handler)
     */
    @Nonnull
    public <T extends URLConnection> T applyAuthentication(@Nonnull T connection) throws AuthenticationException {
        Objects.requireNonNull(connection);

        String encoded = getEncodedAuthenticationValue();

        connection.addRequestProperty(HttpHeaders.AUTHORIZATION, BASIC_AUTH + encoded);

        return connection;
    }

    /**
     * @return The string value to put in the connection's parameters for authentication
     * @throws AuthenticationException
     *             If there is an issue obtaining authentication information. (Required callbacks not supported by
     *             handler, IO issue with handler, any authentication information is not filled in by handler)
     */
    private String getEncodedAuthenticationValue() throws AuthenticationException {
        List<Callback> callbacks = Lists.newLinkedList();

        if (nameCallback.getName() == null) {
            callbacks.add(nameCallback);
        }

        if (passwordCallback.getPassword() == null) {
            callbacks.add(passwordCallback);
        }

        if (!callbacks.isEmpty()) {
            try {
                callbackHandler.handle(callbacks.toArray(new Callback[callbacks.size()]));
            } catch (IOException e) {
                throw new AuthenticationException("IO error retrieving username and password", e);
            } catch (UnsupportedCallbackException e) {
                throw new AuthenticationException("Provided callback handler does not support NameCallback and PasswordCallback", e);
            }
        }

        StringBuilder authenticationBuilder = new StringBuilder();

        String username = nameCallback.getName();
        char[] password = passwordCallback.getPassword();

        if (username == null) {
            throw new AuthenticationException("Callback handler did not populate NameCallback");
        } else if (password == null) {
            throw new AuthenticationException("Callback handler did not populate PasswordCallback");
        }

        authenticationBuilder.append(username).append(':').append(new String(password));

        return Base64.getEncoder().encodeToString(authenticationBuilder.toString().getBytes());
    }
}
