package com.queomedia.base.test.hibernate.contraint;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.queomedia.base.test.hibernate.manytoonelazy.ComponentEntity;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;

@Entity
public class ConstraintEntity extends BusinessEntity<ComponentEntity> {

    /**  The Constant serialVersionUID. */
    private static final long serialVersionUID = 7324445876295621456L;

    private int primitive;

    private String object;

    @NotNull
    private String notNullObject;

    @NotEmpty
    private String notEmptyString;

    /**
     * Constructor used by Hibernate only.
     * 
     * @deprecated This constructor must be only used by Hibernate.
     * It is not really depreciated, but this marker prevents programmers from using the constructor by mistake.
     */
    @Deprecated
    ConstraintEntity() {
        super();
    }

    public ConstraintEntity(final BusinessId<ComponentEntity> bid) {
        super(bid);
    }

    @Override
    public String toString() {
        return "ConstraintEntity [primitive=" + primitive + ", object=" + object + ", notNullObject=" + notNullObject
                + ", notEmptyString=" + notEmptyString + "]";
    }

}
