package com.queomedia.persistence.util;

import java.io.Serializable;
import java.util.Comparator;

import com.queomedia.persistence.BusinessEntity;

/**
 * Compare business entity by there business id.
 *  
 * @author Ralph Engelmann
 *
 * @param <T> the concrete type.
 */ 
public final class BusinessEntityComparator<T extends Serializable> implements Comparator<BusinessEntity<T>>{
    
    /** The holy only one instance. */
    private static final BusinessEntityComparator<Serializable> INSTANCE = new BusinessEntityComparator<Serializable>();
    
    /**
     * The the holy instance.
     * @param <T> the concrete type.
     * @return the Business Id Comparator.
     */
    public static final <T extends Serializable> BusinessEntityComparator<T> getInstance() {        
        /** this cast is save, because it does not matter in anyway. */
        @SuppressWarnings("unchecked")
        BusinessEntityComparator<T> matching = (BusinessEntityComparator<T>) INSTANCE;
        return matching;
    }
    
    /**
     * Use {@link #getInstance()} instead.
     */
    private BusinessEntityComparator() {
        super();
    }

    @Override
    public int compare(BusinessEntity<T> o1, BusinessEntity<T> o2) {
        return o1.getBusinessId().compareTo(o2.getBusinessId());
    }

}
