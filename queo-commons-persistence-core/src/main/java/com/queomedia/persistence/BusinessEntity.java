/**
 * 
 */
package com.queomedia.persistence;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.Hibernate;
import org.springframework.data.domain.Persistable;

import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.BusinessId;

/**
 * The super class of all persistent model classes which have no associated EMP System class.
 * 
 * This class has NO valid equals or hashcode implementation. Use one of its provides specific subclasses instead.
 * 
 * @param <T> the concrete subclass itself, used for the business id
 * @author Engelmann
 */
@SuppressWarnings("serial")
@MappedSuperclass
public abstract class BusinessEntity<T extends Serializable> implements Serializable, Persistable<Long>, BusinessIdOwner<T> {

    /**
     * Hibernate id.
     */
    @Id    
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    /**
     * The Business id of this class.
     */
    @Embedded
    private BusinessId<T> businessId;

    /**
     * Use this constructor only for private constructors of subclasses which are only used by Hibernate.
     */    
    @Deprecated
    protected BusinessEntity() {
    }

    /**
     * The Constructor.
     * 
     * @param businessId the business id
     */
    public BusinessEntity(final BusinessId<T> businessId) {
        Check.notNullArgument(businessId, "businessId");
        this.businessId = businessId;
    }

    /**
     * Used by hibernate
     * @return the hibernate id
     */
    public Long getId() {
        return this.id;
    }
        
        
    protected void setId(Long id) {
        this.id = id;
    }
    
    public Long getHibernateId() {
        return this.id;
    }
    
    public BusinessId<T> getBusinessId() {
        return this.businessId;
    }
    
    protected void setBusinessId(BusinessId<T> businessId) {
        this.businessId = businessId;
    }
    
    public long getBusinessIdValue() {
        return this.businessId.getBusinessId();
    }
    
    @Override
    public boolean isNew() {
        return this.id == null;
    }
        

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.getBusinessId() == null ? 0 : this.getBusinessId().hashCode());
        return result;
    }

    @Override    
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (Hibernate.getClass(this) != Hibernate.getClass(obj)) {
            return false;
        }
        final BusinessEntity<?> other = (BusinessEntity<?>) obj;
        if (this.getBusinessId() == null) {
            if (other.getBusinessId() != null) {
                return false;
            }
        } else if (!this.getBusinessId().equals(other.getBusinessId())) {
            return false;
        }
        return true;
    }

    /**
     * Constructs a <code>String</code> with all attributes
     * in name = value format.
     *
     * @return a <code>String</code> representation 
     * of this object.
     */
    public String toString() {
        final String tab = "    ";
        
        String retValue = "";
        
        retValue = " ( "
            + super.toString() + tab
            + "businessId = " + this.businessId + tab
            + " )";
    
        return retValue;
    }

    
}
