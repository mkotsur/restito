package com.xebialabs.restito.semantics;

import org.glassfish.grizzly.http.server.Response;

/**
 * Something that can be applied to response
 */
@FunctionalInterface
public interface Applicable {
    Response apply(Response r);
}
