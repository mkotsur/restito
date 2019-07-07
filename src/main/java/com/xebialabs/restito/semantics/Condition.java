package com.xebialabs.restito.semantics;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import com.jayway.jsonpath.JsonPath;
import static io.vavr.API.*;

import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import org.apache.mina.util.Base64;
import org.glassfish.grizzly.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.restito.resources.SmartDiscoverer;

import static com.xebialabs.restito.semantics.Action.resourceContent;
import static java.util.Arrays.asList;

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

    private Either<Predicate<Call>, ? extends Seq<Condition>> content;

    protected Option<Applicable> applicable = Option.none();

    private String label;

    protected Condition(Predicate<Call> predicate, String label) {
        this.label = label;
        this.content = Either.left(predicate);
    }

    protected Condition(Predicate<Call> predicate) {
        this.content = Either.left(predicate);
    }

    protected Condition(Predicate<Call> predicate, Applicable applicable) {
        this.content = Either.left(predicate);
        this.applicable = Option.of(applicable);
    }

    protected Condition(Seq<Condition> conditions) {
        this.content = Either.right(conditions);
    }

    protected Condition(List<Condition> conditions, Applicable applicable) {
        this.content = Either.right(io.vavr.collection.List.ofAll(conditions));
        this.applicable = Option.of(applicable);
    }

    /**
     * Returns the predicate of condition
     */
    //TODO: return Optional or remove
//    public Predicate<Call> getPredicate() {
//        return predicate;
//    }

    /**
     * Returns the label of condition
     */
    public Optional<String> getLabel() {
        return Optional.ofNullable(label);
    }

    public Applicable getApplicable() {
        return applicable.getOrElse(Action.noop());
    }

    /**
     * Check if call satisfies the condition
     */
    public boolean check(Call input) {
        return validate(input).isValid();
    }

    private String failureString() {
        return String.format("Condition `%s@%s` failed.", label, hashCode());
    }

    /**
     * Validates an input against the condition and returns:
     * - Valid<Condition> if it matches
     * - Invalid<Seq<String>>
     */
    public Validation<Seq<String>, Condition> validate(Call input) {

        Function<Seq<Condition>, Validation<Seq<String>, Condition>> validateConditions = conditions -> {
            var failedConditions = conditions.filter(c ->
                    !c.validate(input).isValid()
            );
            return failedConditions.isEmpty() ? Validation.valid(this) : Validation.invalid(
                    failedConditions.map(Condition::failureString)
            );
        };

        Function<Predicate<Call>, Validation<Seq<String>, Condition>> validatePredicate = p -> p.test(input) ?
                Validation.valid(this) :
                Validation.invalid(io.vavr.collection.List.of(this.failureString()));

        return content
                .map(validateConditions)
                .mapLeft(validatePredicate)
                .getOrElseGet(Function.identity());
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
    private static Condition methodWithUriAndAutoDiscovery(final Method m, String uri) {
        try {
            final URL resource = new SmartDiscoverer("restito").discoverResource(m, uri);
            return new Condition(List.of(method(m), uri(uri)), resourceContent(resource));
        } catch (IllegalArgumentException e) {
            logger.debug("Can not auto-discover resource for URI [{}]", uri);
        }

        return new Condition(Seq(method(m), uri(uri)));
    }

    /**
     * Checks HTTP parameters. Also work with multi-valued parameters
     */
    public static Condition parameter(final String key, final String... parameterValues) {
        String label = String.format("parameter==%s", Arrays.toString(parameterValues));
        return new Condition(input -> Arrays.equals(input.getParameters().get(key), parameterValues), label);
    }

    /**
     * URI exactly equals to the value returned by {@link org.glassfish.grizzly.http.server.Request#getRequestURI()}
     */
    public static Condition uri(final String uri) {
        String label = String.format("uri==%s", uri);
        return new Condition(input -> input.getUri().equals(uri), label);
    }

    /**
     * URL exactly equals to the value returned by {@link org.glassfish.grizzly.http.server.Request#getRequestURL()}
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
        return c.content
                .mapLeft(p -> new Condition(Predicates.not(p)))
                .map(conditions -> new Condition(conditions.map(Condition::not)))
                .getOrElseGet(Function.identity());
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
    public static Condition withHeader(final String key, final String... expectedHeaders) {
        return new Condition(input -> {
            for (Map.Entry<String, List<String>> e : input.getHeaders().entrySet()) {
                if (e.getKey().equalsIgnoreCase(key)) {
                    return (expectedHeaders == null && e.getValue() == null)
                            || match(e.getValue(), asList(expectedHeaders));
                }
            }
            return false;
        });
    }

    private static boolean match(List<String> incomeHeaders, List<String> expectedValues) {
        return incomeHeaders.containsAll(expectedValues) && expectedValues.containsAll(incomeHeaders);
    }

    /**
     * Method GET with given URI
     */
    public static Condition get(final String uri) {
        return methodWithUriAndAutoDiscovery(Method.GET, uri);
    }

    /**
     * Method POST with given URI
     */
    public static Condition post(String uri) {
        return methodWithUriAndAutoDiscovery(Method.POST, uri);
    }

    /**
     * Method PUT with given URI
     */
    public static Condition put(String uri) {
        return methodWithUriAndAutoDiscovery(Method.PUT, uri);
    }

    /**
     * Method DELETE with given URI
     */
    public static Condition delete(String uri) {
        return methodWithUriAndAutoDiscovery(Method.DELETE, uri);
    }

    /**
     * Method PATCH with given URI
     */
    public static Condition patch(String uri) {
        return methodWithUriAndAutoDiscovery(Method.PATCH, uri);
    }


    /**
     * Always true
     */
    public static Condition alwaysTrue() {
        return custom(Predicates.alwaysTrue());
    }

    /**
     * Always false
     */
    public static Condition alwaysFalse() {
        return custom(Predicates.alwaysFalse());
    }

    public Condition join(Condition condition) {

        final Option<Applicable> actionOption = this.applicable.toArray().appendAll(condition.applicable).foldLeft(
                Option.none(),
                (o, a) -> Option.of(Action.composite(o.getOrElse(Action.noop()), a))
        );
        return new Condition(Seq(this, condition)) {{
            applicable = actionOption;
        }};
    }

    /**
     * Joins many conditions with "and" operation
     */
    // see http://stackoverflow.com/questions/1445233/is-it-possible-to-solve-the-a-generic-array-of-t-is-created-for-a-varargs-param
    @SuppressWarnings("unchecked")
    public static Condition composite(Condition... conditions) {
        var conditionsList = io.vavr.collection.List.of(conditions);
        return conditionsList.foldLeft(alwaysTrue(), Condition::join);
    }


}
