package com.xebialabs.restito.semantics;

import com.google.common.base.Predicate;
import org.glassfish.grizzly.http.Method;
import sun.misc.Regexp;

import javax.annotation.Nullable;
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

	public static Condition method(final Method m) {
		return new Condition(new Predicate<Call>() {
			public boolean apply(Call input) {
				return m.equals(input.getMethod());
			}
		});
	}

	public static Condition parameter(final String key, final String... parameterValues) {
		return new Condition(new Predicate<Call>() {
			public boolean apply(Call input) {
				return Arrays.equals(input.getParameters().get(key), parameterValues);
			}
		});
	}

	public static Condition uri(final String uri) {
		return new Condition(new Predicate<Call>() {
			public boolean apply(Call input) {
				return input.getUri().equals(uri);
			}
		});
	}

	public static Condition endsWithUri(final String uri) {
		return new Condition(new Predicate<Call>() {
			public boolean apply(Call input) {
				return input.getUri().endsWith(uri);
			}
		});
	}

	public static Condition withPostBody() {
		return new Condition(new Predicate<Call>() {
			@Override
			public boolean apply(@Nullable Call input) {
				return input.getPostBody() != null && input.getPostBody().length() > 0;
			}
		});
	}

	public static Condition custom(Predicate<Call> p) {
		return new Condition(p);
	}

	public static Condition withPostBodyContaining(final String str) {
		return new Condition(new Predicate<Call>() {
			@Override
			public boolean apply(Call input) {
				return input.getPostBody() != null && input.getPostBody().contains(str);
			}
		});
	}

	public static Condition withPostBodyContaining(final Regexp regexp) {
		return new Condition(new Predicate<Call>() {
			@Override
			public boolean apply(Call input) {
				return input.getPostBody().matches(regexp.exp);
			}
		});
	}

	public static Condition withHeader(final String key) {
		return new Condition(new Predicate<Call>() {
			@Override
			public boolean apply(Call input) {
				return input.getHeaders().keySet().contains(key);
			}
		});
	}

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
}
