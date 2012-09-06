package com.xebialabs.restito.semantics;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import org.glassfish.grizzly.http.server.Response;

/**
 * When request matches XXX => do YYY
 * Just a wrapper for the Action which will should be performed when Condition is true.
 * Stub is not responsible for decision whether to execute action or not. It's just a wrapper.
 *
 * @see com.xebialabs.restito.semantics.Action
 * @see com.xebialabs.restito.semantics.Condition
 */
public class Stub {

	private Predicate<Call> when = Predicates.alwaysTrue();

	private Function<Response, Response> what = Functions.identity();

	private int appliedTimes;

	public Stub() {}

	public Stub(Predicate<Call> when, Function<Response, Response> what) {
		this.when = when;
		this.what = what;
	}

	public Stub(Condition when, Function<Response, Response> what) {
		this.when = when.getPredicate();
		this.what = what;
	}

	public Stub(Condition when, Action what) {
		this.when = when.getPredicate();
		this.what = what.getFunction();
	}

	public Stub alsoWhen(final Condition extraCondition) {
		final Predicate<Call> currentPredicate = this.when;
		this.when = new Predicate<Call>() {
			public boolean apply(Call input) {
				return extraCondition.check(input) && currentPredicate.apply(input);
			}
		};
		return this;
	}

	public Stub alsoWhat(final Action extraWhat) {
		what = Functions.compose(what, extraWhat.getFunction());
		return this;
	}

	public boolean isApplicable(Call call) {
		return this.when.apply(call);
	}

	public Response apply(Response response) {
		Response newResponse = this.what.apply(response);
		appliedTimes++;
		return newResponse;
	}

	public int getAppliedTimes() {
		return appliedTimes;
	}
}
