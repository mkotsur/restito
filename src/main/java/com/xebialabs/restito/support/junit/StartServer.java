package com.xebialabs.restito.support.junit;

import org.junit.rules.ExternalResource;

import com.xebialabs.restito.server.StubServer;

public class StartServer extends ExternalResource {

    private final StubServer server;

    public StubServer getServer() {
        return this.server;
    }

    public String getServerUrl() {
        return "http://localhost:" + this.server.getPort();
    }

    public StartServer() {
        this.server = new StubServer();
    }

    public StartServer(final int port) {
        this.server = new StubServer(port);
    }

    public StartServer(final int portRangeStart, final int portRangeEnd) {
        this.server = new StubServer(portRangeStart, portRangeEnd);
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        this.server.start();
    }

    @Override
    protected void after() {
        super.after();

        this.server.stop();
    }
}
