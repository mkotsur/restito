package com.xebialabs.restito.builder.stub;

import com.xebialabs.restito.semantics.Applicable;
import com.xebialabs.restito.semantics.Stub;

/**
 * <p>Stub building stage with a sequence attached..</p>
 */
public class StubWithSequence {

    protected Stub stub;

    public StubWithSequence(Stub stub) {
        this.stub = stub;
    }

    /**
     * Should receive requests for all steps of the sequence
     */
    public void mustComplete() {
        stub.setExpectSequenceCompleted(true);
    }


    /**
     * <p>The action to apply for the requests that are exceeding the sequence configuration.</p>
     */
    public StubWithSequence whenExceeded(Applicable action) {
        this.stub.withExceededAction(action);
        return this;
    }

}
