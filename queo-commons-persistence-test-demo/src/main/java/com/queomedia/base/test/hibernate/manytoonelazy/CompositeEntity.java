package com.queomedia.base.test.hibernate.manytoonelazy;

import javax.persistence.Entity;

import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;

@Entity
public class CompositeEntity extends BusinessEntity<CompositeEntity> {
    
    /**  The Constant serialVersionUID. */
    private static final long serialVersionUID = -5329256340896677605L;

    /**
     * Constructor used by Hibernate only.
     * 
     * @deprecated This constructor must be only used by Hibernate.
     * It is not really depreciated, but this marker prevents programmers from using the constructor by mistake.
     */
    @Deprecated
    CompositeEntity() {
        super();
    }
    
    public CompositeEntity(final BusinessId<CompositeEntity> bid) {
       super(bid);
    }
}
