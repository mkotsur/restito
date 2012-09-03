package com.xebialabs.restito;

import com.google.common.collect.Maps;
import com.xebialabs.restito.builder.StubBuilder;
import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Stub;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StubBuilderTest {

	@Mock
	private Response response;

	@Mock
	private Call call;

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

		Stub stub = new StubBuilder().forXmlResourceContent("content.xml").build();

		stub.getWhat().apply(response);


		verify(response).setContentType("application/xml");
		verify(response).setContentLength(13);
		verify(response.getWriter()).write("<test></test>");
	}

	@Test
	public void shouldBuildStubFilteredByExpectedUri() throws Exception {
		Stub stub = new StubBuilder().withUri("/test").build();

		when(call.getUri()).thenReturn("/test");
		assertTrue(stub.getWhen().apply(call));
	}

	@Test
	public void shouldBuildStubForNonExpectedUri() throws Exception {
		Stub stub = new StubBuilder().withUri("/test").build();

		when(call.getUri()).thenReturn("/wrong");
		assertFalse(stub.getWhen().apply(call));
	}

	@Test
	public void shouldBuildStubFilteredByMethod() throws Exception {
		Stub stub4post = new StubBuilder().withMethod(Method.POST).build();
		Stub stub4get = new StubBuilder().withMethod(Method.GET).build();

		when(call.getMethod()).thenReturn(Method.POST);

		assertTrue(stub4post.getWhen().apply(call));
		assertFalse(stub4get.getWhen().apply(call));
	}

	@Test
	public void shouldAggregateConditionsNegative() {
		Stub stub = new StubBuilder().withMethod(Method.POST).withUri("/uri").build();

		when(call.getMethod()).thenReturn(Method.POST);
		when(call.getUri()).thenReturn("/other");
		assertFalse(stub.getWhen().apply(call));

		when(call.getMethod()).thenReturn(Method.GET);
		when(call.getUri()).thenReturn("/uri");
		assertFalse(stub.getWhen().apply(call));
	}

	@Test
	public void shouldAggregateConditionsPositive() {
		Stub stub = new StubBuilder().withMethod(Method.POST).withUri("/uri").build();

		when(call.getMethod()).thenReturn(Method.POST);
		when(call.getUri()).thenReturn("/uri");
		assertTrue(stub.getWhen().apply(call));
	}

	@Test
	public void shouldBuildStubFilteredByPostParameters() {
		Stub stub = new StubBuilder().withParameter("colors", "blue", "green").build();

		Map<String, String[]> colors1 = new HashMap<String, String[]>() {{
			put("colors", new String[]{"blue", "green"});
		}};

		Map<String, String[]> colors2 = new HashMap<String, String[]>() {{
			put("colors", new String[]{"blue", "brown"});
		}};

		when(call.getParameters()).thenReturn(colors1);
		assertTrue(stub.getWhen().apply(call));

		when(call.getParameters()).thenReturn(colors2);
		assertFalse(stub.getWhen().apply(call));

	}

}
