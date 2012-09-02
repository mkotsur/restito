package com.xebialabs.restito.semantics;

import com.google.common.base.Predicate;
import org.glassfish.grizzly.http.Method;

import java.util.Arrays;

public class Condition {

	private Predicate<Call> predicate;

	private Condition(Predicate<Call> predicate) {
		this.predicate = predicate;
	}

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
				return input.getUri().endsWith(uri);
			}
		});
	}

	public static Condition predicate(Predicate<Call> p) {
		return new Condition(p);
	}

	public Predicate<Call> getPredicate() {
		return predicate;
	}
}
