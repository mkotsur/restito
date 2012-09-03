package com.xebialabs.restito.semantics;

import com.google.common.base.Predicate;
import org.glassfish.grizzly.http.Method;
import org.junit.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConditionTest {

	@Mock
	private Call call;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void shouldWorkWithCustomPredicate() {
		Predicate p = mock(Predicate.class);
		assertEquals(p, Condition.predicate(p).getPredicate());
	}

	@Test
	public void shouldDistinguishMethods() {
		Condition condition = Condition.method(Method.GET);

		when(call.getMethod()).thenReturn(Method.POST);
		assertFalse(condition.check(call));

		when(call.getMethod()).thenReturn(Method.GET);
		assertTrue(condition.check(call));
	}

	@Test
	public void shouldDistinguishSingleParameter() {
		Condition condition = Condition.parameter("bar", "foo");

		// Positive
		Map<String, String[]> map1 = paramsMap("bar", "foo");

		when(call.getParameters()).thenReturn(map1);
		assertTrue(condition.check(call));

		// Negative
		map1 = paramsMap("bar", "doo");

		when(call.getParameters()).thenReturn(map1);
		assertFalse(condition.check(call));
	}

	@Test
	public void shouldDistinguishParameterWithArrayValue() {
		Condition condition = Condition.parameter("bar", "foo1", "foo2");

		Map<String, String[]> map1 = paramsMap("bar", "foo1", "foo2");
		when(call.getParameters()).thenReturn(map1);

		assertTrue(condition.check(call));

		map1 = paramsMap("bar", "foo2", "foo1");
		when(call.getParameters()).thenReturn(map1);
		assertFalse(condition.check(call));
	}

	@Test
	public void shouldDistinguishStrictUri() {
		Condition condition = Condition.uri("/boom");

		when(call.getUri()).thenReturn("/boom");
		assertTrue(condition.check(call));

		when(call.getUri()).thenReturn("/big/boom");
		assertFalse(condition.check(call));
	}

	@Test
	public void shouldDistinguishEndsWithUri() {
		Condition condition = Condition.endsWithUri("/boom");

		when(call.getUri()).thenReturn("/boom");
		assertTrue(condition.check(call));

		when(call.getUri()).thenReturn("/big/boom");
		assertTrue(condition.check(call));
	}

	@Test
	public void shouldDistinguishByBodyPresence() {
		Condition condition = Condition.withPostBody();

		assertFalse(condition.check(call));

		when(call.getPostBody()).thenReturn("abrakadabra");
		assertTrue(condition.check(call));
	}


	// Helpers
	private Map<String, String[]> paramsMap(final String key,final  String... values) {
		return new HashMap<String, String[]>() {{
			put(key, values);
		}};
	}
}
