package com.queomedia.persistence.impl;

import org.junit.Assert;
import org.junit.Test;

import com.queomedia.persistence.BusinessId;

public class BusinessIdTest {

    @Test
    public void cloneTest() {
        final long id = 123;
        BusinessId<BusinesseEntityTestImpl> bid = new BusinessId<BusinesseEntityTestImpl>(id);

        /* call method under test */
        BusinessId<BusinesseEntityTestImpl> result1 = bid.clone();

        Assert.assertEquals(id, result1.getBusinessId());
    }
    
    @Test
    public void BusinessIdStringLengthTest() {                
        BusinessId<BusinesseEntityTestImpl> maxBid = new BusinessId<BusinesseEntityTestImpl>(Long.MAX_VALUE);
        BusinessId<BusinesseEntityTestImpl> minBid = new BusinessId<BusinesseEntityTestImpl>(Long.MIN_VALUE);
        BusinessId<BusinesseEntityTestImpl> zeroBid = new BusinessId<BusinesseEntityTestImpl>(0);
                
        Assert.assertEquals(BusinessId.BUSINESS_ID_STRING_LENGTH - 1, maxBid.getAsString().length());
        Assert.assertEquals(BusinessId.BUSINESS_ID_STRING_LENGTH, minBid.getAsString().length());        
        Assert.assertTrue(BusinessId.BUSINESS_ID_STRING_LENGTH >= zeroBid.getAsString().length());
    }
}
