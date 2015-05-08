package com.xebialabs.restito.semantics;

import java.util.Arrays;
import java.util.List;

/**
 * <p><u><b>!EXPERIMENTAL!</b> This stuff is experimental. Which means it may change significantly in future versions.</u></p>
 * <p>Something that can generate actions. This class is for internal usage only.</p>
 * <p>Condition which also contributes some actions a.k.a Applicables (e.g. Stubbed URI may be translated to the resource path which will be added to the response automagically).</p>
 */
public class ConditionWithApplicables extends Condition {

    private List<Applicable> applicables;

    public ConditionWithApplicables(Predicate<Call> predicate, Applicable... applicables) {
        this(predicate, Arrays.asList(applicables));
    }

    public ConditionWithApplicables(Condition condition, Applicable... applicables) {
        this(condition.getPredicate(), Arrays.asList(applicables));
    }

    protected ConditionWithApplicables(Predicate<Call> predicate, List<Applicable> applicables) {
        super(predicate);
        this.applicables = applicables;
    }

    public List<Applicable> getApplicables() {
        return applicables;
    }

}
