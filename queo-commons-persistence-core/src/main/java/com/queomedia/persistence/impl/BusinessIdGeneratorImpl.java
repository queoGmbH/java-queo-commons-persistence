package com.queomedia.persistence.impl;

import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.BusinessIdGenerator;


public class BusinessIdGeneratorImpl implements BusinessIdGenerator {

    /** The uid generator. */
    private UniqueIdGenerator uidGenerator;

    /**
     * Instantiates a new business id generator.
     * 
     * @param uidGenerator the uid generator
     */
    public BusinessIdGeneratorImpl(final UniqueIdGenerator uidGenerator) {
        Check.notNullArgument(uidGenerator, "uidGenerator");

        this.uidGenerator = uidGenerator;
    }
    
    @Override
    public <T extends BusinessEntity<T>> BusinessId<T> generateBusinessId() {
        return new BusinessId<T>(this.uidGenerator.getUID());
    }

    
}
