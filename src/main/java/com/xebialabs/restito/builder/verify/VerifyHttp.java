package com.xebialabs.restito.builder.verify;

import java.util.List;
import com.google.common.collect.Lists;

import com.xebialabs.restito.semantics.Call;
import com.xebialabs.restito.semantics.Condition;
import com.xebialabs.restito.server.StubServer;

import static com.google.common.collect.Iterables.filter;
import static java.lang.String.format;

/**
 * <p>Responsible for building verifications (e.g. something happened x times).</p>
 */
public class VerifyHttp {

    private final List<Call> calls;

    /**
     * Static factory
     */
    public static VerifyHttp verifyHttp(StubServer stubServer) {
        return new VerifyHttp(stubServer.getCalls());
    }

    /**
     * Static factory
     */
    public static VerifyHttp verifyHttp(List<Call> calls) {
        return new VerifyHttp(calls);
    }

    private VerifyHttp(List<Call> calls) {
        this.calls = calls;
    }

    /**
     * There should be only one call which satisfies given conditions
     */
    public VerifySequenced once(Condition... conditions) {
        return times(1, conditions);
    }

    /**
     * There should be no calls which satisfies given conditions
     */
    public VerifySequenced never(Condition... conditions) {
        return times(0, conditions);
    }

    /**
     * There should be <i>t</i> calls which satisfies given conditions
     */
    public VerifySequenced times(int t, Condition... conditions) {
        final List<Call> foundCalls = filterByConditions(conditions);
        if (t != foundCalls.size()) {
            throw new AssertionError(format("Expected to happen %s time(s), but happened %s times instead", t, foundCalls.size()));
        }

        return new VerifySequenced(getCallsAfterLastFound(foundCalls));
    }

    /**
     * There should be at least <i>t</i> calls which satisfies the given conditions
     */
    public VerifySequenced atLeast(int t, Condition... conditions) {
        final List<Call> foundCalls = filterByConditions(conditions);
        if (t > foundCalls.size()) {
            throw new AssertionError(format("Expected to happen at least %s time(s), but happened %s times instead", t, foundCalls.size()));
        }

        return new VerifySequenced(getCallsAfterLastFound(foundCalls));
    }

    private List<Call> getCallsAfterLastFound(final List<Call> foundCalls) {
        return foundCalls.size() == 0 ? calls :
                    calls.subList(
                            calls.indexOf(foundCalls.get(foundCalls.size() - 1)) + 1,
                            calls.size()
                    );
    }

    private List<Call> filterByConditions(Condition[] conditions) {
        List<Call> filteredCalls = calls;

        for (Condition condition : conditions) {
            filteredCalls = Lists.newArrayList(filter(filteredCalls, condition.getPredicate()));
        }
        return filteredCalls;
    }

}
