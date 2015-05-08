package com.xebialabs.restito.semantics;

/**
 * <p>Produce output values based on an input values provided.</p>
 */
public interface Function<I, O> {

    /**
     * Produce result based on passed input
     * @param input - input to process
     * @return resulting value
     */
    O apply(I input);

}
