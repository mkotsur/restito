package com.xebialabs.restito.server;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class StubServerTest {

	@Test
	public void shouldStartServerOnDefaultPort() {
		StubServer server = new StubServer().run();
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
    public void shouldUseSpecificPort() {
        StubServer server = new StubServer(8888).run();
        assertEquals(8888, server.getPort());
    }
}
