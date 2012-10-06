package com.xebialabs.restito.semantics;

import java.net.URL;
import java.util.Arrays;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import com.xebialabs.restito.resources.SmartDiscoverer;

import sun.misc.Regexp;

import static com.xebialabs.restito.semantics.Action.resourceContent;

/**
 * <p>Condition is something that can be true or false given the Call.</p>
 * <p>Also it contains static factory methods. One should feel free to implement own conditions.</p>
 * </p>
 *
 * @see com.xebialabs.restito.semantics.Call
 */
public class Condition {


    private static final Logger logger = LoggerFactory.getLogger(Condition.class);

    protected Predicate<Call> predicate;

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
        return getPredicate().apply(input);
    }

    // Factory methods

    /**
     * Checks HTTP method
     */
    public static Condition method(final Method m) {
        return new Condition(new Predicate<Call>() {
            public boolean apply(Call input) {
                return m.equals(input.getMethod());
            }
        });
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

        return new ConditionWithApplicables(composite(method(m), uri(uri)), Action.custom(Functions.<Response>identity()));
    }

    /**
     * Checks HTTP parameters. Also work with multi-valued parameters
     */
    public static Condition parameter(final String key, final String... parameterValues) {
        return new Condition(new Predicate<Call>() {
            public boolean apply(Call input) {
                return Arrays.equals(input.getParameters().get(key), parameterValues);
            }
        });
    }

    /**
     * URI exactly equals
     */
    public static Condition uri(final String uri) {
        return new Condition(new Predicate<Call>() {
            public boolean apply(Call input) {
                return input.getUri().equals(uri);
            }
        });
    }

    /**
     * URI ends with
     */
    public static Condition endsWithUri(final String uri) {
        return new Condition(new Predicate<Call>() {
            public boolean apply(Call input) {
                return input.getUri().endsWith(uri);
            }
        });
    }

    /**
     * Contains non-empty POST body
     */
    public static Condition withPostBody() {
        return new Condition(new Predicate<Call>() {
            @Override
            public boolean apply(Call input) {
                return input.getPostBody() != null && input.getPostBody().length() > 0;
            }
        });
    }

    /**
     * Custom condition
     */
    public static Condition custom(Predicate<Call> p) {
        return new Condition(p);
    }

    /**
     * With POST body containing string
     */
    public static Condition withPostBodyContaining(final String str) {
        return new Condition(new Predicate<Call>() {
            @Override
            public boolean apply(Call input) {
                return input.getPostBody() != null && input.getPostBody().contains(str);
            }
        });
    }

    /**
     * With post body matching regexp
     */

    public static Condition withPostBodyContaining(final Regexp regexp) {
        return new Condition(new Predicate<Call>() {
            @Override
            public boolean apply(Call input) {
                return input.getPostBody().matches(regexp.exp);
            }
        });
    }

    /**
     * With header present
     */
    public static Condition withHeader(final String key) {
        return new Condition(new Predicate<Call>() {
            @Override
            public boolean apply(Call input) {
                return input.getHeaders().keySet().contains(key);
            }
        });
    }

    /**
     * With header present and equals
     */
    public static Condition withHeader(final String key, final String value) {
        return new Condition(new Predicate<Call>() {
            @Override
            public boolean apply(Call input) {
                String realValue = input.getHeaders().get(key);
                if (realValue == null) {
                    return value == null;
                }
                return realValue.equals(value);
            }
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
     * Joins many conditions with "and" operation
     */
    // see http://stackoverflow.com/questions/1445233/is-it-possible-to-solve-the-a-generic-array-of-t-is-created-for-a-varargs-param
    @SuppressWarnings("unchecked")
    public static Condition composite(Condition... conditions) {
        Condition init = new Condition(Predicates.<Call>alwaysTrue());

        for (Condition condition : conditions) {

            Predicate<Call> newPredicate = Predicates.and(init.getPredicate(), condition.getPredicate());
            if (condition instanceof ConditionWithApplicables) {
                init = new ConditionWithApplicables(newPredicate, ((ConditionWithApplicables) condition).getApplicables());
            } else {
                init = new Condition(newPredicate);
            }
        }

        return init;
    }


}
