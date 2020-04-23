package com.queomedia.persistence.util;

import com.queomedia.commons.equals.EqualsChecker;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;

/**
 * Compare a business id and an business entity by its bussiness id.
 * @author Ralph Engelmann
 *
 * @param <T> the entity class.
 */
public final class BusinessIdEqualsChecker<T extends BusinessEntity<T>> implements EqualsChecker<BusinessId<T>, T> {

    /** The only one instance. */
    @SuppressWarnings("rawtypes")
    private static final BusinessIdEqualsChecker INSTANCE = new BusinessIdEqualsChecker();

    /**
     * Get the instance.
     *
     * @param <T> the generic type
     * @return single instance of BusinessIdEqualsChecker
     */
    @SuppressWarnings("unchecked")
    public static <T extends BusinessEntity<T>> BusinessIdEqualsChecker<T> getInstance() {
        return INSTANCE;
    }

    /** Use {@link #getInstance()} instead. */
    private BusinessIdEqualsChecker() {
        super();
    }

   /*
    * (non-Javadoc)
    *
    * @see com.queomedia.commons.equals.EqualsChecker#equals(java.lang.Object, java.lang.Object)
    */
   @Override
   public boolean equals(final BusinessId<T> businessId, final T businessEntity) {
       if (businessEntity == null) {
           return businessId == null;
       } else {
           return businessEntity.getBusinessId().equals(businessId);
       }
   }

}
