package com.xebialabs.restito.verify;

import com.google.common.base.Predicate;
import com.xebialabs.restito.calls.Call;
import org.glassfish.grizzly.http.Method;

import javax.annotation.Nullable;
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

	public Predicate<Call> getPredicate() {
		return predicate;
	}
}
