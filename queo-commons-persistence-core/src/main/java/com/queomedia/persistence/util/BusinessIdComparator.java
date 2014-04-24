package com.queomedia.persistence.util;

import java.util.Comparator;

import com.queomedia.persistence.BusinessId;

/**
 * Compare business ids.
 * 
 * It is a adapter that makes the {@link java.lang.Comparable} implementation of {@link BusinessId} available for
 * {@link Comparator}.
 * 
 * @author Ralph Engelmann
 *
 * @param <T> the concrete type.
 */ 
public final class BusinessIdComparator<T> implements Comparator<BusinessId<T>>{
    
    /** The holy only one instance. */
    private static final BusinessIdComparator<Object> INSTANCE = new BusinessIdComparator<Object>();
    
    /**
     * The the holy instance.
     * @param <T> the concrete type.
     * @return the Business Id Comparator.
     */
    public static final <T> BusinessIdComparator<T> getInstance() {        
        /** this cast is save, because it does not matter in anyway. */
        @SuppressWarnings("unchecked")
        BusinessIdComparator<T> matching = (BusinessIdComparator<T>) INSTANCE;
        return matching;
    }
    
    /**
     * Use {@link #getInstance()} instead.
     */
    private BusinessIdComparator() {
        super();
    }

    @Override
    public int compare(BusinessId<T> o1, BusinessId<T> o2) {
        return o1.compareTo(o2);
    }

}
