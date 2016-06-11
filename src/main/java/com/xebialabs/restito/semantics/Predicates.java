package com.xebialabs.restito.semantics;

import java.util.function.Predicate;

public class Predicates {
    public static <T> Predicate<T> alwaysTrue() {
        return input -> true;
    }

    public static <T> Predicate<T> alwaysFalse() {
        return input -> false;
    }

    public static <T> Predicate<T> not(final Predicate<T> predicate) {
        return input -> !predicate.test(input);
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> and(final Predicate... predicates) {
        Predicate<T> result = Predicates.alwaysTrue();
        for (Predicate<T> predicate : predicates) {
            result = result.and(predicate);
        }
        return result;
    }
}
