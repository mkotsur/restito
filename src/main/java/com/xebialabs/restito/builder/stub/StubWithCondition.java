package com.xebialabs.restito.builder.stub;

import com.xebialabs.restito.semantics.*;
import com.xebialabs.restito.server.StubServer;

import java.util.Arrays;
import java.util.List;

import static com.xebialabs.restito.semantics.Action.*;

/**
 * <p>Stub building stage with condition attached.</p>
 */
public class StubWithCondition {

    protected Stub stub;

    private StubServer stubServer;


    public StubWithCondition(StubServer stubServer, Condition condition) {
        stub = new Stub(condition);
        this.stubServer = stubServer;
    }

    public StubWithAction then() {
        stubServer.addStub(this.stub);
        return new StubWithAction(stub);
    }


    public StubWithSequence then(ActionSequence... sequences) {

        List<ActionSequence> sequencesList = Arrays.asList(sequences);

        int longestSeqSize = 0;
        for (ActionSequence as : sequencesList) {
            longestSeqSize = as.size() > longestSeqSize ? as.size() : longestSeqSize;
        }

        for(int level = 0; level < longestSeqSize; level++) {
            final int currentLevel = level;
            Action reduce = sequencesList.stream().reduce(noop(), (action, actionSequence) -> {
                if (actionSequence.size() <= currentLevel) {
                    return action;
                }

                return composite(action, actionSequence.getActions().get(currentLevel));
            }, (a1, a2) -> Action.composite(a1, a2));

            stub.withSequenceItem(reduce);
        }

        stubServer.addStub(this.stub);
        return new StubWithSequence(this.stub);
    }

    /**
     * Attach actions to the stub
     */
    public StubWithAction then(Applicable... actions) {
        Stub s = this.stub.withAction(composite(actions));
        stubServer.addStub(s);
        return new StubWithAction(s);
    }

}
