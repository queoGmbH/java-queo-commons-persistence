package com.queomedia.persistence.extra.json.util;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import com.queomedia.commons.checks.Check;

/**
 * The Memorizer Pattern.
 *
 * A Memorizer can calculate a value (V) from an argument (A) with help of an {@link Computable}.
 * The Memorizer will cache the calculated value, and will not do the computation for that argument again.
 *
 * The implementation has some small code style changes, but it works like in the book
 * (Java Concurrency In Practice, page 108).
 *
 *
 * @param <A> the generic type for the argument
 * @param <V> the generic type for the calculated value.
 * @author Brian Goetz and Tim Peierls - in Java Concurrency In Practice, page 108.
 * @author Ralph Engelmann - the small changes
 */
public class Memorizer<A, V> implements Computable<A, V> {

    /** The cache. */
    private final ConcurrentMap<A, Future<V>> cache = new ConcurrentHashMap<>();

    /** The computable. */
    private final Computable<A, V> computable;

    /**
     * Instantiates a new memorizer.
     *
     * @param computable the computable
     */
    public Memorizer(final Computable<A, V> computable) {
        Check.notNullArgument(computable, "computable");

        this.computable = computable;
    }

    @Override
    public V compute(final A argument) throws InterruptedException {
        while (true) {
            Future<V> cacheItem = this.cache.get(argument);
            if (cacheItem == null) {
                Callable<V> eval = new Callable<V>() {
                    @Override
                    public V call() throws InterruptedException {
                        return Memorizer.this.computable.compute(argument);
                    }
                };
                FutureTask<V> futureTask = new FutureTask<>(eval);
                cacheItem = this.cache.putIfAbsent(argument, futureTask);
                if (cacheItem == null) {
                    cacheItem = futureTask;
                    futureTask.run();
                }
            }
            try {
                return cacheItem.get();
            } catch (CancellationException e) {
                this.cache.remove(argument, cacheItem);
            } catch (ExecutionException e) {
                throw launderThrowable(e.getCause());
            }
        }
    }

    /**
     * If the Throwable (parameter) is an Error, throw it; if it is a RuntimeException return it,
     * otherwise throw an IllegalArgumentException.
     * @param throwable the Throwable
     * @return the given Throwable if it is a {@link RuntimeException}.
     */
    private static RuntimeException launderThrowable(final Throwable throwable) {
        Check.notNullArgument(throwable, "throwable");

        if (throwable instanceof RuntimeException) {
            return (RuntimeException) throwable;
        } else if (throwable instanceof Error) {
            throw (Error) throwable;
        } else {
            throw new IllegalArgumentException("Not checked", throwable);
        }
    }

    /**
     * The number of items in the cache.
     * @return cache size.
     */
    public int size() {
        return this.cache.size();
    }
}
