package com.xebialabs.restito.builder.stub;

import com.xebialabs.restito.semantics.Applicable;
import com.xebialabs.restito.semantics.Stub;

/**
 * <p>Stub which already has a sequence attached.</p>
 */
public class StubWithSequence {

    protected Stub stub;

    public StubWithSequence(Stub stub) {
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


    public StubWithSequence whenExceeded(Applicable action) {
        this.stub.withExceededAction(action);
        return this;
    }

}
