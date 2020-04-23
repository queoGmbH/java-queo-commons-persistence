package com.queomedia.persistence.domain;

import java.io.Serializable;
import java.util.Comparator;

import com.queomedia.persistence.BusinessIdOwner;

/**
 * Comparator to compare {@link CreationDateAware} BusinessIdOwners by creation date and, if the title is equal,
 * by businessIds.
 *
 * The ARGUMENTS must also implement the {@link BusinessIdOwner} Interface!
 * @param <T> the generic type
 */
public final class CreationDateComparator<T extends CreationDateAware> implements Comparator<T>, Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 5940088827747107766L;
    
    /** The only one instance. */
    @SuppressWarnings("rawtypes")
    private static final CreationDateComparator INSTANCE = new CreationDateComparator();
    
    /**
     * Return the instance.
     *
     * @param <T> the generic type
     * @return single instance of Title Comparator
     */
    @SuppressWarnings("unchecked")
    public static <T extends CreationDateAware> CreationDateComparator<T> getInstance() {        
        return (CreationDateComparator<T>) INSTANCE;
    }
    
    /** Use {@link #getInstance()} instead. */
    private CreationDateComparator() {
        super();
    }

    @Override
    public int compare(final T o1, final T o2) {
        int titleComparison = o1.getCreationDate().compareTo(o2.getCreationDate());
        if (titleComparison != 0) {
            return titleComparison;
        } else {
            if ((o1 instanceof BusinessIdOwner) && (o2 instanceof BusinessIdOwner)) {
                @SuppressWarnings("rawtypes")
                BusinessIdOwner businessIdOwner1 = (BusinessIdOwner) o1;
                @SuppressWarnings("rawtypes")
                BusinessIdOwner businessIdOwner2 = (BusinessIdOwner) o2;
                @SuppressWarnings("unchecked")
                int bidComparison = businessIdOwner1.getBusinessId().compareTo(businessIdOwner2.getBusinessId());
                return bidComparison;
            }
            return 0;
        }
    }

}
