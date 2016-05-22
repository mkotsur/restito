package com.xebialabs.restito.semantics;

import org.glassfish.grizzly.http.server.Response;

import java.util.Arrays;
import java.util.List;

public class ActionSequence implements ActionLike {

    private List<Applicable> actions;

    private Applicable defaultAction = Action.noop();

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
     * If all passed actions has been already applied it behaves like {@link #defaultAction} action.
     *
     * @param actions queue of actions to be used one by one when {@link Action#apply(Response)} invoked.
     */
    public static ActionSequence sequence(final Applicable... actions) {
        return new ActionSequence(Arrays.asList(actions));
    }

    /**
     * Set the action which will be invoked, when all the other actions from this sequence are exhausted.
     */
    public ActionSequence withDefaultAction(Applicable defaultAction) {
        this.defaultAction = defaultAction;
        return this;
    }

    public List<Applicable> getActions() {
        return actions;
    }

    public Applicable getDefaultAction() {
        return defaultAction;
    }
}
