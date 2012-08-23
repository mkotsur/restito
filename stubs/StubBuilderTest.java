package com.xebialabs.restito.stubs;

import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StubBuilderTest {

	@Mock
	private Response response;

	@Mock
	private Request request;

	@Mock
	private java.io.Writer writer;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(response.getWriter()).thenReturn(writer);
	}


	@Test
	public void shouldBuildSuccessfulStub() {
		Stub stub = new StubBuilder().forSuccess().build();
		stub.getWhat().apply(response);

		verify(response).setStatus(HttpStatus.OK_200);
	}

	@Test
	public void shouldBuildStubWithContent() throws Exception {

		Stub stub = new StubBuilder().forXmlResourceContent("com/xebialabs/restito/stubs/content.xml").build();

		stub.getWhat().apply(response);


		verify(response).setContentType("application/xml");
		verify(response).setContentLength(13);
		verify(response.getWriter()).write("<test></test>");
	}

	@Test
	public void shouldBuildStubFilteredByExpectedUri() throws Exception {
		Stub stub = new StubBuilder().withUri("/test").build();

		when(request.getRequestURI()).thenReturn("/test");
		assertTrue(stub.getWhen().apply(request));
	}

	@Test
	public void shouldBuildStubForNonExpectedUri() throws Exception {
		Stub stub = new StubBuilder().withUri("/test").build();

		when(request.getRequestURI()).thenReturn("/wrong");
		assertFalse(stub.getWhen().apply(request));
	}

	@Test
	public void shouldBuildStubFilteredByMethod() throws Exception {
		Stub stub4post = new StubBuilder().withMethod(Method.POST).build();
		Stub stub4get = new StubBuilder().withMethod(Method.GET).build();

		when(request.getMethod()).thenReturn(Method.POST);

		assertTrue(stub4post.getWhen().apply(request));
		assertFalse(stub4get.getWhen().apply(request));
	}

	@Test
	public void shouldAggregateConditionsNegative() {
		Stub stub = new StubBuilder().withMethod(Method.POST).withUri("/uri").build();

		when(request.getMethod()).thenReturn(Method.POST);
		when(request.getRequestURI()).thenReturn("/other");
		assertFalse(stub.getWhen().apply(request));

		when(request.getMethod()).thenReturn(Method.GET);
		when(request.getRequestURI()).thenReturn("/uri");
		assertFalse(stub.getWhen().apply(request));
	}

	@Test
	public void shouldAggregateConditionsPositive() {
		Stub stub = new StubBuilder().withMethod(Method.POST).withUri("/uri").build();

		when(request.getMethod()).thenReturn(Method.POST);
		when(request.getRequestURI()).thenReturn("/uri");
		assertTrue(stub.getWhen().apply(request));
	}

	@Test
	public void shouldBuildStubFilteredByPostParameters() {
		Stub stub = new StubBuilder().withParameter("colors", "blue", "green").build();

		when(request.getParameterValues("colors")).thenReturn(new String[]{"blue", "green"});
		assertTrue(stub.getWhen().apply(request));

		when(request.getParameterValues("colors")).thenReturn(new String[]{"yellow", "red"});
		assertFalse(stub.getWhen().apply(request));

	}

}
