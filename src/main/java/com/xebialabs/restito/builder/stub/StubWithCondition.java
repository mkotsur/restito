package com.xebialabs.restito.builder.stub;

import com.xebialabs.restito.semantics.*;
import com.xebialabs.restito.server.StubServer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.xebialabs.restito.semantics.Action.*;

/**
 * Next step in stub building. New actions can be attached.
 */
public class StubWithCondition {

    protected Stub stub;

    private StubServer stubServer;


    public StubWithCondition(StubServer stubServer, Condition condition) {
        stub = new Stub(condition);
        this.stubServer = stubServer;
    }

    // TODO fix this API!
    public StubWithAction then(ActionSequence... sequences) {

        List<ActionSequence> sequencesList = Arrays.asList(sequences);

        int longestSeqSize = 0;
        for (ActionSequence as : sequencesList) {
            longestSeqSize = as.sequenceSize() > longestSeqSize ? as.sequenceSize() : longestSeqSize;
        }

        for(int level = 0; level < longestSeqSize; level++) {
            final int currentLevel = level;
            Action reduce = sequencesList.stream().reduce(noop(), (action, actionSequence) -> {
                if (actionSequence.sequenceSize() <= currentLevel) {
                    return action;
                }

                return composite(action, actionSequence.getActions().get(currentLevel));
            }, (a1, a2) -> Action.composite(a1, a2));

            stub.withSequenceItem(reduce);
        }

        stubServer.addStub(this.stub);
        return new StubWithAction(this.stub);
    }

    /**
     * Attach actions to the stub
     */
    public StubWithAction then(Applicable... actions) {
        Stub s = this.stub.withAction(composite(actions));
//        List<Applicable> actionsList = Arrays.asList(actions);

//        int longestSeqSize = 0;
//        for (ActionLike action : actions) {
//            if (action.sequenceSize() > longestSeqSize) {
//                longestSeqSize = action.sequenceSize();
//            }
//        }
//
//
//        Action combinedFlatActions = actionsList.stream()
//                .filter(a -> a instanceof Action)
//                .map(a -> (Action)a)
//                .reduce(noop(), (a, acc) -> composite(a, acc));
//
//
//        List<ActionSequence> actionSequences = actionsList.stream()
//                .filter(a -> a instanceof ActionSequence)
//                .map(a -> (ActionSequence) a)
//                .collect(Collectors.toList());
//
//        for(int level = 0; level < longestSeqSize; level++) {
//            final int currentLevel = level;
//            Action reduce = actionSequences.stream().reduce(combinedFlatActions, (action, actionSequence) -> {
//                if (actionSequence.sequenceSize() <= currentLevel) {
//                    return action;
//                }
//
//                return composite(action, actionSequence.getActions().get(currentLevel));
//            }, (a1, a2) -> Action.composite(a1, a2));
//
//            stub.withSequenceItem(reduce);
//        }


        stubServer.addStub(s);
        return new StubWithAction(s);
    }

}
