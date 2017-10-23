package com.xebialabs.restito.semantics;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.jayway.jsonpath.JsonPath;
import org.apache.mina.util.Base64;
import org.glassfish.grizzly.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.restito.resources.SmartDiscoverer;

import static com.xebialabs.restito.semantics.Action.resourceContent;

/**
 * <p>Condition is something that can be true or false given the Call.</p>
 * <p>Also it contains static factory methods. One should feel free to implement own conditions.</p>
 *
 * Note, that terms <b>URL</b> and <b>URI</b> have the same meaning as in {@link org.glassfish.grizzly.http.server.Request}.
 *
 * @see com.xebialabs.restito.semantics.Call
 */
@SuppressWarnings("SameParameterValue,")
public class Condition {

    private static final Logger logger = LoggerFactory.getLogger(Condition.class);

    private Predicate<Call> predicate;

    protected Condition(Predicate<Call> predicate) {
        this.predicate = predicate;
    }

    /**
     * Returns the predicate of condition
     */
    public Predicate<Call> getPredicate() {
        return predicate;
    }

    /**
     * Check if call satisfies the condition
     */
    public boolean check(Call input) {
        return getPredicate().test(input);
    }

    // Factory methods

    /**
     * Checks HTTP method
     */
    public static Condition method(final Method m) {
        return new Condition(input -> m.equals(input.getMethod()));
    }

    /**
     * Checks HTTP method, URI and enables AutoDiscovery
     */
    private static ConditionWithApplicables methodWithUriAndAutoDiscovery(final Method m, String uri) {
        try {
            final URL resource = new SmartDiscoverer("restito").discoverResource(m, uri);
            return new ConditionWithApplicables(composite(method(m), uri(uri)), resourceContent(resource));
        } catch (IllegalArgumentException e) {
            logger.debug("Can not auto-discover resource for URI [{}]", uri);
        }

        return new ConditionWithApplicables(composite(method(m), uri(uri)), Action.noop());
    }

    /**
     * Checks HTTP parameters. Also work with multi-valued parameters
     */
    public static Condition parameter(final String key, final String... parameterValues) {
        return new Condition(input -> Arrays.equals(input.getParameters().get(key), parameterValues));
    }

    /**
     * Checks HTTP parameter present.
     */
    public static Condition hasParameter(final String key) {
        return new Condition(input -> input.getParameters().containsKey(key));
    }

    /**
     * URI exactly equals to the value returned by {@link org.glassfish.grizzly.http.server.Request#getRequestURI()}
     */
    public static Condition uri(final String uri) {
        return new Condition(input -> input.getUri().equals(uri));
    }

    /**
     * URL exactly equals to the value returned by {@link org.glassfish.grizzly.http.server.Request#getRequestURL)}
     */
    public static Condition url(final String url) {
        return new Condition(input -> input.getUrl().equals(url));
    }

    /**
     * URI ends with
     */
    public static Condition endsWithUri(final String uri) {
        return new Condition(input -> input.getUri().endsWith(uri));
    }

    /**
     * URI starts with
     */
    public static Condition startsWithUri(final String uri) {
        return new Condition(input -> input.getUri().startsWith(uri));
    }

    /**
     * URI starts with
     */
    public static Condition matchesUri(final Pattern p) {
        return new Condition(input -> input.getUri().matches(p.pattern()));
    }

    /**
     * Contains non-empty POST body
     */
    public static Condition withPostBody() {
        return new Condition(input -> input.getPostBody() != null && input.getPostBody().length() > 0);
    }

    /**
     * If basic authentication is provided
     */
    public static Condition basicAuth(String username, String password) {
        final String authString = username + ":" + password;
        final String encodedAuthString = new String(Base64.encodeBase64(authString.getBytes()));
        return new Condition(input -> ("Basic " + encodedAuthString).equals(input.getAuthorization()));
    }

    /**
     * Custom condition
     */
    public static Condition custom(Predicate<Call> p) {
        return new Condition(p);
    }

    /**
     * Not condition
     */
    public static Condition not(Condition c) {
        return new Condition(Predicates.not(c.getPredicate()));
    }

    /**
     * With POST body containing string
     */
    public static Condition withPostBodyContaining(final String str) {
        return new Condition(input -> input.getPostBody() != null && input.getPostBody().contains(str));
    }

    /**
     * With post body matching pattern
     */
    public static Condition withPostBodyContaining(final Pattern pattern) {
        return new Condition(input -> input.getPostBody().matches(pattern.pattern()));
    }

    /**
     * With Valid Json Path.
     * Check to see if incoming path has a valid string value selected via a <a href="https://github.com/jayway/JsonPath/">
     * JSONPath</a> expression.
     *
     * @param - Json Path
     * @param - value to check against
     * @return
     */
    public static Condition withPostBodyContainingJsonPath(final String pattern, final Object value) {
        return new Condition(input -> value.equals(JsonPath.parse(input.getPostBody()).read(pattern)));
    }

    /**
     * With header present.
     * Header key is case insensitive. More information <a href="http://stackoverflow.com/questions/5258977/are-http-headers-case-sensitive">here</a>.
     */
    public static Condition withHeader(final String key) {
        return new Condition(input -> {
            for (String k : input.getHeaders().keySet()) {
                if (k.equalsIgnoreCase(key)) return true;
            }
            return false;
        });
    }

    /**
     * With header present and equals.
     * Header key is case insensitive. More information <a href="http://stackoverflow.com/questions/5258977/are-http-headers-case-sensitive">here</a>.
     */
    public static Condition withHeader(final String key, final String value) {
        return new Condition(input -> {
            for (Map.Entry<String, String> e : input.getHeaders().entrySet()) {
                if (e.getKey().equalsIgnoreCase(key)) {
                    return (value == null && e.getValue() == null) || e.getValue().equals(value);
                }
            }
            return false;
        });
    }

    /**
     * With header present and contains all values.
     * Header key is case insensitive. More information <a href="http://stackoverflow.com/questions/5258977/are-http-headers-case-sensitive">here</a>.
     */
    public static Condition withMultiHeaderContains(final String key, final String... values) {
        return new Condition(input -> {
            final String[] actualHeaderValues = input.getMultiHeaders().get(key);
            return Optional.ofNullable(actualHeaderValues)
                           .filter(it -> Stream.of(values).allMatch(val -> Arrays.binarySearch(it, val) >= 0))
                           .isPresent();
        });
    }

    /**
     * With header present and matches all values. Header should contain exact number of elements. Order does not matter.
     * Header key is case insensitive. More information <a href="http://stackoverflow.com/questions/5258977/are-http-headers-case-sensitive">here</a>.
     */
    public static Condition withMultiHeaderMatches(final String key, final String... values) {
        return new Condition(input -> {
            final String[] actualHeaderValues = input.getMultiHeaders().get(key);
            return Optional.ofNullable(actualHeaderValues)
                           .filter(it -> it.length == values.length)
                           .filter(it -> Stream.of(values).allMatch(val -> Arrays.binarySearch(it, val) >= 0))
                           .isPresent();
        });
    }

    /**
     * Method GET with given URI
     */
    public static ConditionWithApplicables get(final String uri) {
        return methodWithUriAndAutoDiscovery(Method.GET, uri);
    }

    /**
     * Method POST with given URI
     */
    public static ConditionWithApplicables post(String uri) {
        return methodWithUriAndAutoDiscovery(Method.POST, uri);
    }

    /**
     * Method PUT with given URI
     */
    public static ConditionWithApplicables put(String uri) {
        return methodWithUriAndAutoDiscovery(Method.PUT, uri);
    }

    /**
     * Method DELETE with given URI
     */
    public static ConditionWithApplicables delete(String uri) {
        return methodWithUriAndAutoDiscovery(Method.DELETE, uri);
    }

    /**
     * Method PATCH with given URI
     */
    public static ConditionWithApplicables patch(String uri) {
        return methodWithUriAndAutoDiscovery(Method.PATCH, uri);
    }


    /**
     * Always true
     */
    public static Condition alwaysTrue() {
        return custom(Predicates.<Call>alwaysTrue());
    }

    /**
     * Always false
     */
    public static Condition alwaysFalse() {
        return custom(Predicates.<Call>alwaysFalse());
    }

    /**
     * Joins many conditions with "and" operation
     */
    // see http://stackoverflow.com/questions/1445233/is-it-possible-to-solve-the-a-generic-array-of-t-is-created-for-a-varargs-param
    @SuppressWarnings("unchecked")
    public static Condition composite(Condition... conditions) {
        Condition init = alwaysTrue();

        for (Condition condition : conditions) {
            Predicate<Call> newPredicate = Predicates.and(init.getPredicate(), condition.getPredicate());
            if (condition instanceof ConditionWithApplicables) {
                init = new ConditionWithApplicables(newPredicate, ((ConditionWithApplicables) condition).getApplicables());
            } else {
                init = Condition.custom(newPredicate);
            }
        }

        return init;
    }


}
