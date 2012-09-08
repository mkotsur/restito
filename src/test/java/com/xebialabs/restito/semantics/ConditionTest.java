package com.xebialabs.restito.semantics;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import org.glassfish.grizzly.http.Method;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sun.misc.Regexp;

import java.util.HashMap;
import java.util.Map;

import static com.xebialabs.restito.semantics.Condition.endsWithUri;
import static junit.framework.Assert.*;
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
		Predicate<Call> p = Predicates.alwaysTrue();
		assertEquals(p, Condition.custom(p).getPredicate());
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

	@Test
	public void shouldDistinguishByStringInBody() {
		Condition condition = Condition.withPostBodyContaining("abra");

		assertFalse(condition.check(call));

		when(call.getPostBody()).thenReturn("abrakadabra");
		assertTrue(condition.check(call));

		condition = Condition.withPostBodyContaining("sweets");
		assertFalse(condition.check(call));
	}

	@Test
	public void shouldDistinguishByRegexpMatchInBody() {
		Condition condition = Condition.withPostBodyContaining(new Regexp("[0-9]+"));

		when(call.getPostBody()).thenReturn("331102");
		assertTrue(condition.check(call));

		condition = Condition.withPostBodyContaining(new Regexp("[a-z]+"));
		assertFalse(condition.check(call));

	}

	@Test
	public void shouldDistinguishByHeaderPresence() {
		Condition withFoo = Condition.withHeader("foo");
		Condition withFooContainsBar = Condition.withHeader("foo", "bar");

		when(call.getHeaders()).thenReturn(Maps.<String, String>newHashMap());

		assertFalse(withFoo.check(call));
		assertFalse(withFooContainsBar.check(call));

		when(call.getHeaders()).thenReturn(header("foo", "bar"));

		assertTrue(withFoo.check(call));
		assertTrue(withFooContainsBar.check(call));
	}

	@Test
	public void shouldCreateCompositeCondition() {
		Condition catTomcatCondition = Condition.composite(endsWithUri("cat"), endsWithUri("tomcat"));

		when(call.getUri()).thenReturn("/cat");
		assertFalse(catTomcatCondition.check(call));

		when(call.getUri()).thenReturn("/tomcat");
		assertTrue(catTomcatCondition.check(call));
	}

	// Helpers
	private Map<String, String[]> paramsMap(final String key,final  String... values) {
		return new HashMap<String, String[]>() {{
			put(key, values);
		}};
	}

	private Map<String, String> header(final String key,final String value) {
		return new HashMap<String, String>() {{
			put(key, value);
		}};
	}
}
