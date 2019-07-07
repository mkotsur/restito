package com.xebialabs.restito.semantics;

import com.xebialabs.restito.builder.ensure.EnsureHttp;
import org.glassfish.grizzly.http.server.Response;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.xebialabs.restito.semantics.Action.composite;
import static com.xebialabs.restito.semantics.Action.noop;

/**
 * <p>Stub is not responsible for decision whether to execute action or not (e.g. when request matches `XXX` - do `YYY`)</p>
 * <p>Just a wrapper for the {@link Action} which will should be performed when {@link Condition} is true.</p>
 *
 * @see com.xebialabs.restito.semantics.Action
 * @see com.xebialabs.restito.semantics.Condition
 */
public class Stub {

    private Condition when;

    private Applicable action = noop();

    private List<Applicable> actionSequence = new CopyOnWriteArrayList<>();

    private Applicable exceededAction = null;

    private int appliedTimes = 0;

    /**
     * How many times stub has been called
     */
    private int expectedTimes = 0;

    /**
     * Should the sequence be completed or not in order to pass {@link EnsureHttp#gotStubsCommitmentsDone}
     */
    private Boolean expectSequenceCompleted = false;

    /**
     * Label, which is used in the error messages
     */
    private String label;

    /**
     * Creates a stub with action and condition
     */
    public Stub(Condition when, Applicable action) {
        this.when = when;
        this.action = action;
    }

    public Stub(Condition when) {
        this.when = when;
    }

    /**
     * Creates a stub with action and condition
     */
    public Stub(Condition when, ActionSequence actionSequence) {
        this.when = when;
        this.actionSequence.addAll(actionSequence.getActions());
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
    public Stub withExtraAction(final Applicable extraAction) {
       action = composite(action, extraAction);
        return this;
    }

    public Stub withSequenceItem(final Applicable nextWhat) {
        actionSequence.add(nextWhat);
        return this;
    }

    /**
     * Checks whether the call satisfies condition of this stub
     */
    public boolean isApplicable(Call call) {
        return when.validate(call).isValid() && (actionSequence.size() == 0 || exceededAction != null || actionSequence.size() > appliedTimes);
    }

    /**
     * Executes all actions against the response.
     */
    public Response apply(final Response response) {
        Response appliedResponse = when.getApplicable().map(a -> a.apply(response)).getOrElse(response);

        Applicable chosenAction;

        if (actionSequence.isEmpty()) {
            chosenAction = action;
        } else if (actionSequence.size() > appliedTimes) {
            chosenAction = composite(action, actionSequence.get(appliedTimes));
        } else if(exceededAction != null) {
            chosenAction = exceededAction;
        } else {
            chosenAction = action;
        }

        appliedResponse = chosenAction.apply(appliedResponse);
        appliedTimes++;
        return appliedResponse;
    }

    public Stub withAction(Applicable action) {
        this.action = action;
        return this;
    }

    public Stub withActionSequence(List<Applicable> actionSequence) {
        this.actionSequence = new CopyOnWriteArrayList<>(actionSequence);
        return this;
    }

    public Stub withExceededAction(Applicable exceededAction) {
        this.exceededAction = exceededAction;
        return this;
    }

    public int getAppliedTimes() {
        return appliedTimes;
    }

    public void setExpectedTimes(int expectedTimes) {
        this.expectedTimes = expectedTimes;
    }

    public void setExpectSequenceCompleted(Boolean expectSequenceCompleted) {
        this.expectSequenceCompleted = expectSequenceCompleted;
    }

    public List<Applicable> getActionSequence() {
        return actionSequence;
    }

    public Boolean getExpectSequenceCompleted() {
        return expectSequenceCompleted;
    }

    /**
     * Get how many times stub expected to be called
     */
    public int getExpectedTimes() {
        return expectedTimes;
    }

    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder("Stub@" + this.hashCode());
        if (label != null) {
            toString.append(" [").append(label).append("]");
        }

        return toString.toString();
    }

    public Stub withLabel(String label) {
        this.label = label;
        return this;
    }
}
