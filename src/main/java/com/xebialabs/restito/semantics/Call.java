package com.xebialabs.restito.semantics;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;

/**
 * <p>Call that happened to server. Handy wrapper for Request values.</p>
 */
public class Call {

    private Method method;
    private String uri;
    private String contentType;
    private String postBody;
    private String url;
    private String authorization;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String[]> parameters = new HashMap<>();
    private Request request;

    private Call() {
    }

    /**
     * Factory method
     */
    public static Call fromRequest(final Request request) {
        Call call = new Call();

        call.request = request;
        call.method = request.getMethod();
        call.authorization = request.getAuthorization();
        call.uri = request.getRequestURI();
        call.contentType = request.getContentType();
        call.url = request.getRequestURL().toString();

        for (String s : request.getHeaderNames()) {
            call.headers.put(s, request.getHeader(s));
        }

        call.parameters = new HashMap<>(request.getParameterMap());

        try {
            call.postBody = request.getPostBody(999999).toStringContent(Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException("Problem reading Post Body of HTTP call");
        }

        return call;
    }

    /**
     * URI of the call
     */
    public String getUri() {
        return uri;
    }

    /**
     * Content type of the call
     */
    public String getContentType() {
        return contentType;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Map of headers
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Http method
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Map of parameters. All parameters considered as if they were multi-valued.
     */
    public Map<String, String[]> getParameters() {
        return parameters;
    }

    /**
     * In case of POST request - returns post body
     */
    public String getPostBody() {
        return postBody;
    }

    /**
     * Returns an authorization header, or null if doesn't exist
     */
    public String getAuthorization() {
        return authorization;
    }

    /**
     * Returns raw HTTP request
     */
    public Request getRequest() {
        return request;
    }
}


