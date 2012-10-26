package com.xebialabs.restito.builder.stub;

import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;

/**
 * Normal HTTP stub
 */
public class StubHttp {

    private StubServer stubServer;

    private StubHttp(StubServer stubServer) {
        this.stubServer = stubServer;
    }

    /**
     * Static factory for this class
     */
    public static StubHttp whenHttp(StubServer server) {
        return new StubHttp(server);
    }

    /**
     * Adds some conditions to the stub
     */
    public StubActioned match(Condition... conditions) {
        return new StubActioned(stubServer, Condition.composite(conditions));
    }
}
