package com.xebialabs.restito.stubs;

import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.junit.*;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class StubTest {

	@Test
	public void shouldBuildProperBaseWithInitialValues() {
		Request request = mock(Request.class);
		Response response = mock(Response.class);

		Stub stub = new Stub();
		assertEquals(false, stub.getWhen().apply(request));
		assertEquals(response, stub.getWhat().apply(response));
	}
}
