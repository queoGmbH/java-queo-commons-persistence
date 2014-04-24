package com.queomedia.persistence.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;

public class BusinessIdEqualsCheckerTest {

    /** Entity used for this test. */
    public static class DemoEntity extends BusinessEntity<DemoEntity> {
        private static final long serialVersionUID = 5600118403143652210L;

        @Deprecated
        DemoEntity() {
            super();
        }

        public DemoEntity(final BusinessId<DemoEntity> businessId) {
            super(businessId);
        }
    }

    @Test
    public void testEquals() {
        DemoEntity entity = new DemoEntity(new BusinessId<DemoEntity>(123));

        assertTrue(BusinessIdEqualsChecker.<DemoEntity> getInstance().equals(entity.getBusinessId(), entity));
        assertFalse(BusinessIdEqualsChecker.<DemoEntity> getInstance().equals(new BusinessId<DemoEntity>(1234543),
                entity));
    }

    @Test
    public void testEqualsWithNull() {
        DemoEntity entity = new DemoEntity(new BusinessId<DemoEntity>(123));

        assertTrue(BusinessIdEqualsChecker.<DemoEntity> getInstance().equals(null, null));
        assertFalse(BusinessIdEqualsChecker.<DemoEntity> getInstance()
                .equals(new BusinessId<DemoEntity>(1234543), null));
        assertFalse(BusinessIdEqualsChecker.<DemoEntity> getInstance().equals(null, entity));
    }

}
