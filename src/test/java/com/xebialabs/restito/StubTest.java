package com.xebialabs.restito;

import com.xebialabs.restito.semantics.Stub;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class StubTest {

	@Test
	public void shouldBuildProperBaseWithInitialValues() {
		Request request = mock(Request.class);
		Response response = mock(Response.class);

		Stub stub = new Stub();
		assertTrue(stub.getWhen().apply(request));
		assertEquals(response, stub.getWhat().apply(response));
	}
}
