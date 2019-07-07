package com.xebialabs.restito.semantics;

import io.vavr.collection.Seq;
import io.vavr.control.Option;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * <p><u><b>!EXPERIMENTAL!</b> This stuff is experimental. Which means it may change significantly in future versions.</u></p>
 * <p>Something that can generate actions. This class is for internal usage only.</p>
 * <p>Condition which also contributes some actions a.k.a Applicables (e.g. Stubbed URI may be translated to the resource path which will be added to the response automagically).</p>
 */
public class ConditionWithApplicables extends Condition {

    protected ConditionWithApplicables(Seq<Condition> conditions, Applicable applicable) {
        super(conditions);
        super.applicable = Option.of(applicable);
    }

    public ConditionWithApplicables(Predicate<Call> predicate, Applicable applicable) {
        super(predicate);
        super.applicable = Option.of(applicable);
    }
}
