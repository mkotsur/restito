package com.xebialabs.restito.builder.stub;

import com.xebialabs.restito.server.StubServer;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class StubHttpTest {

	@Test
	public void shouldReturnProperChainObject() {
		assertNotNull(StubHttp.whenHttp(mock(StubServer.class)));
	}
}
