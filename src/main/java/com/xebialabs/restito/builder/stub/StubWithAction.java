package com.xebialabs.restito.builder.stub;

import com.xebialabs.restito.semantics.ActionSequence;
import com.xebialabs.restito.semantics.Applicable;
import com.xebialabs.restito.semantics.Stub;

/**
 * <p>Stub building stage with actions attached.</p>
 */
public class StubWithAction {

    protected Stub stub;

    public StubWithAction(Stub stub) {
        this.stub = stub;
    }

    /**
     * Should happen once
     */
    public void mustHappen() {
        stub.setExpectedTimes(1);
    }

    /**
     * Set number of times it should happen
     */
    public void mustHappen(int times) {
        stub.setExpectedTimes(times);
    }

    public StubWithSequence withSequence(Applicable... actions) {
        stub.withActionSequence(ActionSequence.sequence(actions).getActions());
        return new StubWithSequence(stub);
    }
}
