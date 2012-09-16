package com.xebialabs.restito.semantics;

import org.glassfish.grizzly.http.server.Response;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * When request matches XXX => do YYY
 * Just a wrapper for the Action which will should be performed when Condition is true.
 * Stub is not responsible for decision whether to execute action or not. It's just a wrapper.
 *
 * @see com.xebialabs.restito.semantics.Action
 * @see com.xebialabs.restito.semantics.Condition
 */
public class Stub {

	private Condition when = Condition.custom(Predicates.<Call>alwaysTrue());

	private Action what = Action.custom(Functions.<Response>identity());

	private int appliedTimes = 0;

	private int expectedTimes = 0;

	public Stub() {}

    @Deprecated
	public Stub(Predicate<Call> when, Function<Response, Response> what) {
		this.when = Condition.custom(when);
		this.what = Action.custom(what);
	}

    @Deprecated
	public Stub(Condition when, Function<Response, Response> what) {
		this.when = when;
		this.what = Action.custom(what);
	}

	public Stub(Condition when, Action what) {
		this.when = when;
		this.what = what;
	}

	public Stub alsoWhen(final Condition extraCondition) {
		this.when = Condition.composite(this.when, extraCondition);
		return this;
	}

	public Stub alsoWhat(final Action extraWhat) {
        what = Action.composite(what, extraWhat);
		return this;
	}

	public boolean isApplicable(Call call) {
		return this.when.getPredicate().apply(call);
	}

	public Response apply(Response response) {
        if (when instanceof ConditionWithApplicables) {
            for (Applicable applicable : ((ConditionWithApplicables) when).getApplicables()) {
                response = applicable.apply(response);
            }
        }

		response = this.what.apply(response);
		appliedTimes++;
		return response;
	}

	public int getAppliedTimes() {
		return appliedTimes;
	}

	public void setExpectedTimes(int expectedTimes) {
		this.expectedTimes = expectedTimes;
	}

	public int getExpectedTimes() {
		return expectedTimes;
	}
}
