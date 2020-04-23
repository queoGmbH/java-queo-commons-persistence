package com.queomedia.persistence.util;

import com.queomedia.persistence.BusinessIdOwner;

/**
 * Util class for {@link BusinessIdOwner}.
 */
public class BusinessIdOwnerUtil {
    
    /**
     * Util classes need no constructor.
     */
    private BusinessIdOwnerUtil() {
        super();
    }
    
    /**
     * Compare by bid if both objects are instance of {@link BusinessIdOwner}, otherwise return 0.
     *
     * @param o1 the o 1
     * @param o2 the o 2
     * @return the int
     */
    public static int compareByBidWhenPossible(final Object o1, final Object o2) {
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
