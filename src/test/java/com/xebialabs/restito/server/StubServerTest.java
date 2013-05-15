package com.xebialabs.restito.server;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class StubServerTest {

    private StubServer server;

    @Before
    public void start() {
        server = new StubServer().run();
    }

    @After
    public void stop() {
        server.stop();
    }

    @Test
    public void shouldStartServerOnDefaultPort() {
        assertEquals(6666, server.getPort());
        server.stop();
    }

    @Test
    public void shouldStartServerOnRandomPortWhenDefaultPortIsBusy() {
        StubServer server1 = new StubServer().run();
        StubServer server2 = new StubServer().run();
        assertTrue(server2.getPort() > server1.getPort());
    }

    @Test
    public void shouldBePossibleToSpecifyPort() {
        StubServer server = new StubServer(8888).run();
        assertEquals(8888, server.getPort());
        server.stop();
    }
}
