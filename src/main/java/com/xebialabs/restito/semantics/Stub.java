package com.xebialabs.restito.semantics;

import org.glassfish.grizzly.http.server.Response;
import com.google.common.base.Functions;
import com.google.common.base.Predicates;

/**
 * <p>Stub is not responsible for decision whether to execute action or not (e.g. when request matches XXX => do YYY)</p>
 * <p>Just a wrapper for the {@link Action} which will should be performed when {@link Condition} is true.</p>
 *
 * @see com.xebialabs.restito.semantics.Action
 * @see com.xebialabs.restito.semantics.Condition
 */
public class Stub {

	private Condition when = Condition.custom(Predicates.<Call>alwaysTrue());

	private Action what = Action.custom(Functions.<Response>identity());

	private int appliedTimes = 0;

	private int expectedTimes = 0;

    /**
     * Creates a stub with action and condition
     */
	public Stub(Condition when, Action what) {
		this.when = when;
		this.what = what;
	}

    /**
     * Appends an extra condition to the stub.
     */
	public Stub alsoWhen(final Condition extraCondition) {
		this.when = Condition.composite(this.when, extraCondition);
		return this;
	}

    /**
     * Appends an extra action to the stub
     */
	public Stub alsoWhat(final Action extraWhat) {
        what = Action.composite(what, extraWhat);
		return this;
	}

    /**
     * Checks whether the call satisfies condition of this stub
     */
	public boolean isApplicable(Call call) {
		return this.when.getPredicate().apply(call);
	}

    /**
     * Executes all actions against the response.
     */
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

    /**
     * How many times stub has been called
     */
	public int getAppliedTimes() {
		return appliedTimes;
	}

    /**
     * Set how many times stub expected to be called
     */
	public void setExpectedTimes(int expectedTimes) {
		this.expectedTimes = expectedTimes;
	}

    /**
     * Get how many times stub expected to be called
     */
	public int getExpectedTimes() {
		return expectedTimes;
	}
}
