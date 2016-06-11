package com.xebialabs.restito.semantics;

import org.glassfish.grizzly.http.server.Response;

import java.util.Arrays;
import java.util.List;

public class ActionSequence {

    private List<Applicable> actions;

    public ActionSequence(List<Applicable> actions) {
        this.actions = actions;
    }

    public int size() {
        return actions.size();
    }

    /**
     * Creates a sequence action which contains all passed actions and
     * executes one by one of them in the same order if {@link Action#apply(Response)} is repeated.
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
