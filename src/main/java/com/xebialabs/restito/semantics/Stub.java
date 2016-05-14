package com.xebialabs.restito.semantics;

import org.glassfish.grizzly.http.server.Response;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

import static com.xebialabs.restito.semantics.Action.noop;

/**
 * <p>Stub is not responsible for decision whether to execute action or not (e.g. when request matches XXX => do YYY)</p>
 * <p>Just a wrapper for the {@link Action} which will should be performed when {@link Condition} is true.</p>
 *
 * @see com.xebialabs.restito.semantics.Action
 * @see com.xebialabs.restito.semantics.Condition
 */
public class Stub {

    private Condition when = Condition.custom(Predicates.<Call>alwaysTrue());

    private List<Applicable> what = new CopyOnWriteArrayList<>();

    private int appliedTimes = 0;

    private int expectedTimes = 0;

    /**
     * Creates a stub with action and condition
     */
    public Stub(Condition when, Action what) {
        this.when = when;
        this.what.add(what);
    }

    public Stub(Condition when) {
        this.when = when;
    }

    /**
     * Creates a stub with action and condition
     */
    public Stub(Condition when, ActionSequence what) {
        this.when = when;
        this.what.addAll(what.getActions());
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
    public Stub alsoWhat(final Applicable extraWhat) {
        //TODO remove?
        what.replaceAll(new UnaryOperator<Applicable>() {
            @Override
            public Applicable apply(Applicable action) {
                return Action.composite(action, extraWhat);
            }
        });
        return this;
    }

    public Stub thenWhat(final Applicable nextWhat) {
        what.add(nextWhat);
        return this;
    }

    /**
     * Checks whether the call satisfies condition of this stub
     */
    public boolean isApplicable(Call call) {
        return when.getPredicate().apply(call) && (what.size() == 1 || what.size() > appliedTimes);
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

        Applicable action;

        if (what.size() == 1) {
            action = what.get(0);
        } else if (what.size() > appliedTimes) {
            action = what.get(appliedTimes);
        } else {
            action = noop();
        }

        response = action.apply(response);
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


// OK_200, SEQ(1, 2), SEQ(A, B)


// OK_200, 1, A
// OK_200,