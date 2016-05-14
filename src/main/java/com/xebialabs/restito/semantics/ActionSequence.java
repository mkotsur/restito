package com.xebialabs.restito.semantics;

import org.glassfish.grizzly.http.server.Response;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ActionSequence implements ActionLike {

    private List<Applicable> actions;

    public ActionSequence(List<Applicable> actions) {
        this.actions = actions;
    }

    @Override
    public int sequenceSize() {
        return actions.size();
    }

    /**
     * Creates a sequence action which contains all passed actions and
     * executes one by one of them in the same order if {@link Action#apply(Response)} is repeated.
     * If all passed actions has been already applied it behaves like {@link Action#noop()} action.
     *
     * @param actions queue of actions to be used one by one when {@link Action#apply(Response)} invoked.
     */
    public static ActionSequence sequence(final Applicable... actions) {
        return new ActionSequence(Arrays.asList(actions));
    }

    public List<Applicable> getActions() {
        return actions;
    }
}
