package com.xebialabs.restito.semantics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.glassfish.grizzly.http.Method;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.xebialabs.restito.semantics.Condition.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class ConditionTest {

    @Mock
    private Call call;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

//    @Test
//    public void shouldWorkWithCustomPredicate() {
//        Predicate<Call> p = Predicates.alwaysTrue();
//        assertEquals(p, Condition.custom(p).getPredicate());
//    }

    @Test
    public void shouldWorkWithNot() {
        Predicate<Call> p = Predicates.alwaysTrue();
        Condition t = Condition.custom(p);
        assertFalse(Condition.not(t).check(call));
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
    public void shouldDistinguishStartsWithUri() {
        Condition condition = Condition.startsWithUri("/big");

        when(call.getUri()).thenReturn("/big/boom");
        assertTrue(condition.check(call));

        when(call.getUri()).thenReturn("/boom").getMock();
        assertFalse(condition.check(call));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldMakeConditionForUriMatching() {

        Condition condition = Condition.matchesUri(Pattern.compile("^/[0-9]*"));

        when(call.getUri()).thenReturn("/232323");
        assertTrue(condition.check(call));

        when(call.getUri()).thenReturn("/boom").getMock();
        assertFalse(condition.check(call));
    }

    @Test
    public void shouldMakeConditionForUriPatternMatch() {
        Condition condition = Condition.matchesUri(Pattern.compile("^/[0-9]*"));

        when(call.getUri()).thenReturn("/232323");
        assertTrue(condition.check(call));

        when(call.getUri()).thenReturn("/boom").getMock();
        assertFalse(condition.check(call));
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
    public void shouldDistinguishByStringInJsonPath() {
        Condition condition = Condition.withPostBodyContainingJsonPath("$.name", "foo");

        when(call.getPostBody()).thenReturn("{\"name\":\"foo\"}");
        assertTrue(condition.check(call));

        condition = Condition.withPostBodyContainingJsonPath("$.name","bar");
        assertFalse(condition.check(call));
    }

    @Test
    public void shouldDistinguishNumberByStringInJsonPath() {
        Condition condition = Condition.withPostBodyContainingJsonPath("$.age", 5);

        when(call.getPostBody()).thenReturn("{\"age\": 5}");
        assertTrue(condition.check(call));

        condition = Condition.withPostBodyContainingJsonPath("$.age",6);
        assertFalse(condition.check(call));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldDistinguishByRegexpMatchInBody() {
        Condition condition = Condition.withPostBodyContaining(Pattern.compile("[0-9]+"));

        when(call.getPostBody()).thenReturn("331102");
        assertTrue(condition.check(call));

        condition = Condition.withPostBodyContaining(Pattern.compile("[a-z]+"));
        assertFalse(condition.check(call));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void shouldDistinguishByPatternMatchInBody() {
        Condition condition = Condition.withPostBodyContaining(Pattern.compile("[0-9]+"));

        when(call.getPostBody()).thenReturn("331102");
        assertTrue(condition.check(call));

        condition = Condition.withPostBodyContaining(Pattern.compile("[a-z]+"));
        assertFalse(condition.check(call));
    }

    @Test
    public void shouldDistinguishByHeaderPresence() {
        Condition withFoo = Condition.withHeader("foo");
        Condition withFooContainsBar = Condition.withHeader("foo", "bar");

        when(call.getHeaders()).thenReturn(new HashMap<>());

        assertFalse(withFoo.check(call));
        assertFalse(withFooContainsBar.check(call));

        when(call.getHeaders()).thenReturn(header("foo", "bar"));

        assertTrue(withFoo.check(call));
        assertTrue(withFooContainsBar.check(call));
    }

    @Test
    public void headersShouldBeCaseInsensitive() {
        when(call.getHeaders()).thenReturn(header("foo", "bar"));
        assertTrue(Condition.withHeader("fOo").check(call));
        assertTrue(Condition.withHeader("fOo", "bar").check(call));
        assertFalse(Condition.withHeader("fOo", "bAr").check(call));
    }

    @Test
    public void shouldCreateCompositeCondition() {
        Condition catTomcatCondition = Condition.composite(method(Method.POST), endsWithUri("tomcat"));

        when(call.getUri()).thenReturn("/tomcat");
        when(call.getMethod()).thenReturn(Method.GET);
        assertFalse(catTomcatCondition.check(call));

        when(call.getUri()).thenReturn("/tomcat");
        when(call.getMethod()).thenReturn(Method.POST);
        assertTrue(catTomcatCondition.check(call));
    }

    @Test
    public void shouldCreateGetPostPutDeleteWithUriConditions() {
        Condition get = get("/get");
        Condition post = post("/post");
        Condition put = put("/put");
        Condition delete = delete("/delete");

        when(call.getMethod()).thenReturn(Method.GET);
        when(call.getUri()).thenReturn("/get");


        assertTrue(get.check(call));
        assertFalse(post.check(call));
        assertFalse(put.check(call));
        assertFalse(delete.check(call));

        when(call.getMethod()).thenReturn(Method.GET);
        when(call.getUri()).thenReturn("/post");

        assertFalse(get.check(call));
        assertFalse(post.check(call));
        assertFalse(put.check(call));
        assertFalse(delete.check(call));

        when(call.getMethod()).thenReturn(Method.POST);
        when(call.getUri()).thenReturn("/post");

        assertFalse(get.check(call));
        assertTrue(post.check(call));
        assertFalse(put.check(call));
        assertFalse(delete.check(call));

        when(call.getMethod()).thenReturn(Method.DELETE);
        when(call.getUri()).thenReturn("/delete");

        assertFalse(get.check(call));
        assertFalse(post.check(call));
        assertFalse(put.check(call));
        assertTrue(delete.check(call));

        when(call.getMethod()).thenReturn(Method.PUT);
        when(call.getUri()).thenReturn("/put");

        assertFalse(get.check(call));
        assertFalse(post.check(call));
        assertTrue(put.check(call));
        assertFalse(delete.check(call));
    }

    @Test
    public void shouldHaveLabelForUri() {
        String label = uri("https://google.com").getLabel().get();
        assertEquals("uri==https://google.com", label);
    }

    @Test
    public void shouldReturnReasonWhySimpleConditionFailed() {
        Condition uri = uri("https://google.com");
        when(call.getUri()).thenReturn("https://yahoo.com");
        Validation<Seq<String>, Condition> validate = uri.validate(call);
        assertTrue(validate.isInvalid());

        String expectedError = String.format("Condition `%s@%s` failed.", uri.getLabel().get(), uri.hashCode());
        assertEquals(io.vavr.collection.List.of(expectedError), validate.getError());
    }

    @Test
    public void shouldReturnValidWhenSimpleConditionPasses() {
        Condition uri = uri("https://google.com");
        when(call.getUri()).thenReturn("https://google.com");
        Validation<Seq<String>, Condition> validate = uri.validate(call);
        assertTrue(validate.isValid());
        assertEquals(Validation.valid(uri), validate);
    }

    @Test
    public void shouldReturnReasonsWhyComplexConditionFailed() {
        Condition uri = uri("https://google.com");
        Condition parameter = parameter("foo", "bar");
        Condition complex = Condition.composite(uri, parameter);
        when(call.getUri()).thenReturn("https://yahoo.com");
        when(call.getParameters()).thenReturn(Map.of());

        Validation<Seq<String>, Condition> validate = complex.validate(call);
        assertTrue(validate.isInvalid());

        String expectedError1 = String.format("Condition `%s@%s` failed.", uri.getLabel().get(), uri.hashCode());
        String expectedError2 = String.format("Condition `%s@%s` failed.", parameter.getLabel().get(), parameter.hashCode());
        assertEquals(io.vavr.collection.List.of(expectedError1, expectedError2), validate.getError());
    }

    // Helpers
    private Map<String, String[]> paramsMap(final String key, final String... values) {
        return new HashMap<String, String[]>() {{
            put(key, values);
        }};
    }

    private Map<String, List<String>> header(final String key, final String value) {
        return new HashMap<String, List<String>>() {{
            put(key, Arrays.asList(value));
        }};
    }
}
