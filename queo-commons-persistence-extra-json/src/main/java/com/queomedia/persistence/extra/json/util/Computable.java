package com.queomedia.persistence.extra.json.util;

/**
 * A function that computes a value (V) from an argument (A).
 * It is part of the Memorizer Pattern.
 *
 * @param <A> the generic type for the argument
 * @param <V> the generic type for the calculated value.
 * @author Brian Goetz and Tim Peierls - in Java Concurrency In Practice, page 108.
 */
public interface Computable<A, V> {

    /**
     * Compute a value.
     *
     * @param argument the argument
     * @return the computed value
     * @throws InterruptedException because of threading.
     */
    V compute(A argument) throws InterruptedException;
}
