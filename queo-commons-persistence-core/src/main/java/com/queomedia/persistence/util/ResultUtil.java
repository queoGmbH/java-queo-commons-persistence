package com.queomedia.persistence.util;

import java.util.Collection;

import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotFoundRuntimeException;

/**
 * Until methods to handle query results.
 * @author Ralph Engelmann
 *
 */
public final class ResultUtil {

    /**
     * Util classes need no constructor.
     */
    private ResultUtil() {
        super();
    }

    /**
     * Extract the one result from the collection, or if the collection is empty returns null.
     * But if the collection contains more then one element an {@link IncorrectResultSizeDataAccessException}
     * is thrown.
     *
     * @param <T> the generic type
     * @param oneOrZeroElementResultSet the collection if one ore zero elements
     * @return the one object or null
     * @throws IncorrectResultSizeDataAccessException if the collection contains more than one elment
     */
    public static <T> T requiredOneOrNoResult(Collection<T> oneOrZeroElementResultSet)
            throws IncorrectResultSizeDataAccessException {
        Check.notNullArgument(oneOrZeroElementResultSet, "oneElementOrEmptyResultSet");

        int size = oneOrZeroElementResultSet.size();
        if (size == 0) {
            return null;
        }
        if (size > 1) {
            throw new IncorrectResultSizeDataAccessException(1, size);
        }
        return oneOrZeroElementResultSet.iterator().next();
    }

    /**
     * Extract the one result from the collection, or if the collection is empty returns null.
     * But if the collection contains more then one element an {@link IncorrectResultSizeDataAccessException}
     * is thrown.
     *
     * @param <T> the generic type
     * @param oneOrZeroElementResultSet the collection if one ore zero elements
     * @param errorMessage the error message
     * @return the one object
     * @throws NotFoundRuntimeException the not found runtime exception
     * @throws IncorrectResultSizeDataAccessException if the collection contains more than one element
     */
    public static <T> T requiredOneResult(Collection<T> oneOrZeroElementResultSet, String errorMessage)
            throws NotFoundRuntimeException, IncorrectResultSizeDataAccessException {
        Check.notNullArgument(oneOrZeroElementResultSet, "oneElementOrEmptyResultSet");

        int size = oneOrZeroElementResultSet.size();
        if (size == 0) {
            throw new NotFoundRuntimeException(errorMessage);
        }
        if (size > 1) {
            throw new IncorrectResultSizeDataAccessException(1, size);
        }
        return oneOrZeroElementResultSet.iterator().next();
    }

}
