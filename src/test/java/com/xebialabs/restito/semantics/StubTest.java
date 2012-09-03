package com.xebialabs.restito.semantics;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.glassfish.grizzly.http.server.Response;
import org.junit.Test;

import javax.annotation.Nullable;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class StubTest {

	@Test
	public void shouldBuildProperBaseWithInitialValues() {
		Call call = mock(Call.class);
		Response response = mock(Response.class);

		Stub stub = new Stub();
		assertTrue(stub.getWhen().apply(call));
		assertEquals(response, stub.getWhat().apply(response));
	}


	@Test
	public void shouldHandleCallAsWhen() {
		final Call r = mock(Call.class);
		when(r.getUri()).thenReturn("/boom");

		Stub stub = new Stub(
				new Predicate<Call>() {
					@Override
					public boolean apply(@Nullable Call input) {
						return r.getUri().equals("/boom");
					}
				},
				Functions.<Response>identity()
		);

		assertTrue(stub.getWhen().apply(r));
	}

	@Test
	public void shouldDoWhat() {

		Response r = mock(Response.class);

		Stub stub = new Stub(
				Predicates.<Call>alwaysTrue(),
				new Function<Response, Response>() {
					@Override
					public Response apply(@Nullable Response input) {
						input.setContentType("boo");
						return input;
					}
				}
		);

		stub.getWhat().apply(r);

		verify(r, times(1)).setContentType("boo");

	}
}

