package com.xebialabs.restito.semantics;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import org.glassfish.grizzly.http.Method;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.xebialabs.restito.semantics.Condition.delete;
import static com.xebialabs.restito.semantics.Condition.endsWithUri;
import static com.xebialabs.restito.semantics.Condition.get;
import static com.xebialabs.restito.semantics.Condition.method;
import static com.xebialabs.restito.semantics.Condition.post;
import static com.xebialabs.restito.semantics.Condition.put;
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

    @Test
    public void shouldWorkWithCustomPredicate() {
        Predicate<Call> p = Predicates.alwaysTrue();
        assertEquals(p, Condition.custom(p).getPredicate());
    }

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
    public void shouldDistinguishParameterPresence() throws Exception {
        Map<String, String[]> map1 = paramsMap("bar", "foo1", "foo2");
        when(call.getParameters()).thenReturn(map1);

        assertTrue(Condition.hasParameter("bar").check(call));
        assertFalse(Condition.hasParameter("baz").check(call));
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

        when(call.getHeaders()).thenReturn(new HashMap<String, String>());

        assertFalse(withFoo.check(call));
        assertFalse(withFooContainsBar.check(call));

        when(call.getHeaders()).thenReturn(header("foo", "bar"));

        assertTrue(withFoo.check(call));
        assertTrue(withFooContainsBar.check(call));
    }

    @Test
    public void shouldDistinguishByMultiHeaderHeader() {
        Condition withFzzContainsBar = Condition.withMultiHeaderContains("fzz", "bar");
        Condition withEmptyFzz = Condition.withMultiHeaderContains("fzz");
        Condition withFooContainsBarBaz = Condition.withMultiHeaderContains("foo", "bar", "baz");
        Condition withFooContainsBazBar = Condition.withMultiHeaderContains("fOo", "baz", "bar");
        Condition withFooContainsBar = Condition.withMultiHeaderContains("foo", "bar");
        Condition withFooContainsBas = Condition.withMultiHeaderContains("foo", "bas");
        Condition withZzzContainsBar = Condition.withMultiHeaderContains("zzz", "bar");

        Condition withFooMatchesBarBaz = Condition.withMultiHeaderMatches("foo", "bar", "baz");
        Condition withFooMatchesBar = Condition.withMultiHeaderMatches("foo", "bar");

        when(call.getMultiHeaders())
                .thenReturn(multiHeader("fzz"))
                .thenReturn(multiHeader("fzz"))
                .thenReturn(multiHeader("foo", "bar", "baz"));

        assertTrue(withEmptyFzz.check(call));
        assertFalse(withFzzContainsBar.check(call));

        assertTrue(withFooContainsBarBaz.check(call));
        assertTrue(withFooContainsBazBar.check(call));
        assertTrue(withFooContainsBar.check(call));
        assertFalse(withFooContainsBas.check(call));
        assertFalse(withZzzContainsBar.check(call));

        assertTrue(withFooMatchesBarBaz.check(call));
        assertFalse(withFooMatchesBar.check(call));
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

    // Helpers
    private Map<String, String[]> paramsMap(final String key, final String... values) {
        return new HashMap<String, String[]>() {{
            put(key, values);
        }};
    }

    private Map<String, String> header(final String key, final String value) {
        return new HashMap<String, String>() {{
            put(key, value);
        }};
    }

    private Map<String, String[]> multiHeader(final String key, final String... values) {
        final TreeMap<String, String[]> map = new TreeMap<String, String[]>(String.CASE_INSENSITIVE_ORDER);
        map.put(key, values);
        return map;
    }
}
