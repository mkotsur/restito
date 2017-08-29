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
    public StubWithCondition match(Condition... conditions) {
        return new StubWithCondition(stubServer, Condition.composite(conditions));
    }

    /**
     * Creates a stub with a label, and adds a condition to it
     */
    public StubWithCondition match(String label, Condition... conditions) {
        StubWithCondition swc = new StubWithCondition(stubServer, Condition.composite(conditions));
        swc.stub.withLabel(label);
        return swc;
    }
}
