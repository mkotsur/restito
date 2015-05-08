package com.xebialabs.restito.semantics;

import java.util.ArrayList;
import java.util.List;

public class Predicates {
    public static <T> Predicate<T> alwaysTrue() {
        return new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return true;
            }
        };
    }

    public static <T> Predicate<T> alwaysFalse() {
        return new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return false;
            }
        };
    }

    public static <T> Predicate<T> not(final Predicate<T> predicate) {
        return new Predicate<T>() {
            @Override
            public boolean apply(T input) {
                return !predicate.apply(input);
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Predicate<T> and(final Predicate... predicates) {
        return new Predicate<T>() {
            private List<Predicate<T>> composed = new ArrayList<Predicate<T>>() {{
                for (Predicate predicate : predicates) {
                    add(predicate);
                }
            }};

            @Override
            public boolean apply(T input) {
                boolean result = true;
                for (Predicate<T> predicate : composed) {
                    result = result && predicate.apply(input);
                }
                return result;
            }
        };
    }
}
