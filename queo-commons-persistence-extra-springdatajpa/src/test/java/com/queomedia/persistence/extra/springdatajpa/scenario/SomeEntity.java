package com.queomedia.persistence.extra.springdatajpa.scenario;

import javax.persistence.Entity;

import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;

/** A almost empty entity */
@Entity
public class SomeEntity extends BusinessEntity<SomeEntity> {

    /**  The Constant serialVersionUID. */
    private static final long serialVersionUID = -5822780904267477907L;

    /**
     * Constructor used by Hibernate only.
     * 
     * @deprecated This constructor must be only used by Hibernate.
     * It is not really depreciated, but this marker prevents programmers from using the constructor by mistake.
     */
    @Deprecated
    SomeEntity() {
        super();
    }
    
    public SomeEntity(BusinessId<SomeEntity> bid) {
        super(bid);
    }
    
}
