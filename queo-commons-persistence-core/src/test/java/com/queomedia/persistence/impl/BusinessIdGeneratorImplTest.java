package com.queomedia.persistence.impl;


import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.queomedia.persistence.BusinessId;

public class BusinessIdGeneratorImplTest {

    @Test
    public void testGenerateBusinessId() {
        BusinessIdGeneratorImpl classUnderTest = new BusinessIdGeneratorImpl(new UniqueIdGenerator());
        
        /** call method under test */
        BusinessId<BusinesseEntityTestImpl> bid1 = classUnderTest.generateBusinessId();
        BusinessId<BusinesseEntityTestImpl> bid2 = classUnderTest.generateBusinessId();
        
        assertFalse(bid1.equals(bid2));
        
    }

}
