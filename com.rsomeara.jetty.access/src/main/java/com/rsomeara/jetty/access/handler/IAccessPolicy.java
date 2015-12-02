package com.rsomeara.jetty.access.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

/**
 * Policy which determines how requests that are allowed or denied from proceeding are handled
 *
 * <p>
 * Various access handling may check request parameters and allow or deny access to a servlet. An access policy
 * determines what action the application will take in addition to allowing/denying access to the servlet. A simple
 * denial policy, for example, might do something such as send a '403 - FORBIDDEN' response code
 * </p>
 *
 * @author romeara
 * @since 0.1
 */
public interface IAccessPolicy {

    /**
     * Called when a request is prevented from proceeding based on the access control configured
     *
     * @param target
     *            The target of the request - either a URI or a name.
     * @param baseRequest
     *            The original unwrapped request object.
     * @param request
     *            The request either as the {@link Request} object or a wrapper of that request. The
     *            <code>{@link HttpConnection#getCurrentConnection()}.{@link HttpConnection#getHttpChannel()
     *             getHttpChannel()}.{@link HttpChannel#getRequest() getRequest()}</code>
     *            method can be used access the Request object if required.
     * @param response
     *            The response as the {@link Response} object or a wrapper of that request. The
     *            <code>{@link HttpConnection#getCurrentConnection()}.{@link HttpConnection#getHttpChannel()
     *             getHttpChannel()}.{@link HttpChannel#getResponse() getResponse()}</code>
     *            method can be used access the Response object if required.
     * @throws IOException
     *             if unable to handle the request or response processing
     * @since 0.1
     */
    public void handleDenied(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * Called when a request is allowed to proceed based on the access control configured
     *
     * @param target
     *            The target of the request - either a URI or a name.
     * @param baseRequest
     *            The original unwrapped request object.
     * @param request
     *            The request either as the {@link Request} object or a wrapper of that request. The
     *            <code>{@link HttpConnection#getCurrentConnection()}.{@link HttpConnection#getHttpChannel()
     *             getHttpChannel()}.{@link HttpChannel#getRequest() getRequest()}</code>
     *            method can be used access the Request object if required.
     * @param response
     *            The response as the {@link Response} object or a wrapper of that request. The
     *            <code>{@link HttpConnection#getCurrentConnection()}.{@link HttpConnection#getHttpChannel()
     *             getHttpChannel()}.{@link HttpChannel#getResponse() getResponse()}</code>
     *            method can be used access the Response object if required.
     * @throws IOException
     *             if unable to handle the request or response processing
     * @since 0.1
     */
    public void handleAllowed(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
