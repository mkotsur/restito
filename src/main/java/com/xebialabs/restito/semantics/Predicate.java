package com.xebialabs.restito.semantics;

/**
 * <p>Predicate is a function that accepts input and returns 'true' or 'false' depending on the input.</p>
 */
public interface Predicate<T> {

    /**
     * Analyze input and return boolean conclusion
     * @param input - input to be analyzed
     * @return conclusion
     */
    boolean apply(T input);

}
