package com.xebialabs.restito.semantics;

import java.util.List;
import com.google.common.base.Predicate;

import static com.google.common.collect.Lists.newArrayList;

/**
 * Something that can generate actions. This class is for internal usage only.
 */
public class ConditionWithApplicables extends Condition {

    private List<Applicable> applicables;

    public ConditionWithApplicables(Predicate<Call> predicate, Applicable... applicables) {
        this(predicate, newArrayList(applicables));
    }
    public ConditionWithApplicables(Condition condition, Applicable... applicables) {
        this(condition.getPredicate(), newArrayList(applicables));
    }

    protected ConditionWithApplicables(Predicate<Call> predicate, List<Applicable> applicables) {
        super(predicate);
        this.applicables = applicables;
    }

    public List<Applicable> getApplicables() {
        return applicables;
    }

}
