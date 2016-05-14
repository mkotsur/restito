package com.xebialabs.restito.builder.stub;

import com.xebialabs.restito.semantics.*;
import com.xebialabs.restito.server.StubServer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.xebialabs.restito.semantics.Action.*;

/**
 * Next step in stub building. New actions can be attached.
 */
public class StubActioned extends StubExpected {

    private StubServer stubServer;


    public StubActioned(StubServer stubServer, Condition condition) {
        super(new Stub(condition));
        this.stubServer = stubServer;
    }

    /**
     * Attach actions to the stub
     */
    public StubExpected then(ActionLike... actions) {
        Stub s = this.stub;

        List<ActionLike> actionsList = Arrays.asList(actions);

        int longestSeqSize = 0;
        for (ActionLike action : actions) {
            if (action.sequenceSize() > longestSeqSize) {
                longestSeqSize = action.sequenceSize();
            }
        }


        Action combinedFlatActions = actionsList.stream()
                .filter(a -> a instanceof Action)
                .map(a -> (Action)a)
                .reduce(noop(), (a, acc) -> composite(a, acc));


        List<ActionSequence> actionSequences = actionsList.stream()
                .filter(a -> a instanceof ActionSequence)
                .map(a -> (ActionSequence) a)
                .collect(Collectors.toList());

        for(int level = 0; level < longestSeqSize; level++) {
            final int currentLevel = level;
            Action reduce = actionSequences.stream().reduce(combinedFlatActions, (action, actionSequence) -> {
                if (actionSequence.sequenceSize() <= currentLevel) {
                    return action;
                }

                return composite(action, actionSequence.getActions().get(currentLevel));
            }, (a1, a2) -> Action.composite(a1, a2));

            stub.thenWhat(reduce);
        }


        stubServer.addStub(s);
        return new StubExpected(s);
    }

}
