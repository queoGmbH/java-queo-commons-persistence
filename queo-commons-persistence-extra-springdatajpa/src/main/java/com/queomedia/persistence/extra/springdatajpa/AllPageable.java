package com.queomedia.persistence.extra.springdatajpa;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

/**
 * Construct {@link Pageable}s that have no SQL-Limit in a fluent way.
 * Use the static methods {@link #sortedBy(Sort)}, {@code sorted(Direction, String...)} and {@link #unsorted()} for
 * creation.
 */
public final class AllPageable {

    /** Util classes need no constructor. */
    private AllPageable() {
        super();
    }

    /**
     * Instantiates a new all {@link Pageable} with sort parameters applied.
     *
     * @param sort the sort
     * @return the Pagable
     */
    public static Pageable sortedBy(final Sort sort) {
        return PageRequest.of(0, Integer.MAX_VALUE, sort);
    }

    /**
     * Creates a new all {@link Pageable} with sort parameters applied.
     *
     * @param direction the sort order
     * @param properties the properties
     * @return the Pagable
     */
    public static Pageable sorted(final Direction direction, final String... properties) {
        return PageRequest.of(0, Integer.MAX_VALUE, direction, properties);
    }

    /**
     * Instantiates a new all {@link Pageable} without sorting.
     *
     * Consider to use {@link Pageable#unpaged()} instead.
     *
     * @return the Pagable
     */
    public static Pageable unsorted() {
        return PageRequest.of(0, Integer.MAX_VALUE);
    }
}
