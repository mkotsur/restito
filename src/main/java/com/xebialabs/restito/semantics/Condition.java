package com.xebialabs.restito.semantics;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.glassfish.grizzly.http.Method;
import sun.misc.Regexp;

import java.util.Arrays;

/**
 * Condition is something that can be true or false given the Call.
 * Also it contains static factory methods.
 * One should feel free to implement own conditions.
 *
 * @see com.xebialabs.restito.semantics.Call
 */
public class Condition {

	private Predicate<Call> predicate;

	private Condition(Predicate<Call> predicate) {
		this.predicate = predicate;
	}

	public Predicate<Call> getPredicate() {
		return predicate;
	}

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
	public static Condition get(String uri) {
		return composite(method(Method.GET), uri(uri));
	}

	/**
	 * Method POST with given URI
	 */
	public static Condition post(String uri) {
		return composite(method(Method.POST), uri(uri));
	}

	/**
	 * Method PUT with given URI
	 */
	public static Condition put(String uri) {
		return composite(method(Method.PUT), uri(uri));
	}

	/**
	 * Method DELETE with given URI
	 */
	public static Condition delete(String uri) {
		return composite(method(Method.DELETE), uri(uri));
	}

	/**
	 * Joins many conditions with "and" operation
	 */
	// see http://stackoverflow.com/questions/1445233/is-it-possible-to-solve-the-a-generic-array-of-t-is-created-for-a-varargs-param
	@SuppressWarnings("unchecked")
	public static Condition composite(Condition... conditions) {
		Predicate<Call> init = Predicates.alwaysTrue();

		for (Condition condition : conditions) {
			init = Predicates.and(init, condition.predicate);
		}

		return new Condition(init);
	}


}
