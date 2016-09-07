package com.queomedia.persistence;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

import org.hibernate.Hibernate;

/**
 * A business ID for a specific class.
 * 
 * @param <T> where the business id is for.
 * @author Engelmann
 */
@Embeddable
public class BusinessId<T> implements Serializable, Cloneable, Comparable<BusinessId<T>> {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -2834540074806131641L;
    
    /**
     * The length of an business id when it is expressed in decimal style.
     */
    public static final int BUSINESS_ID_STRING_LENGTH = 20; 

    /** The business id. */
    @Column(nullable = false, unique = true, name = "businessId")
    private long businessId;

    /** This useless field is nessary for the uselesse flex remoting. */
    @Transient
    private String internalLongAsString;

    /**
     * Used by Hibernate.
     * @deprecated only for reflection api's usage. dont't use it directly or ralph hurts you.
     */
    @Deprecated
    public BusinessId() {
    }

    /**
     * Instantiates a new business id.
     * 
     * @param businessID the business id
     */
    public BusinessId(final long businessID) {
        this.businessId = businessID;
    }

    /**
     * Gets the business id.
     * 
     * @return the business id
     */
    public long getBusinessId() {
        return this.businessId;
    }

    /**
     * Used by Hibernate.
     * 
     * @param businessId the business id
     */
    protected void setBusinessId(final long businessId) {
        this.businessId = businessId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.businessId ^ this.businessId >>> 32);
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
        final BusinessId<?> other = (BusinessId<?>) obj;
        if (this.businessId != other.getBusinessId()) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "bid=" + this.businessId;
    }

    /**
     * Return the internal business ID as String.
     * @return the internal business ID
     * @deprecated Used only for flex remoting, because flex does not support 64 bit longs.
     */
    @Deprecated
    public String getInternalLongAsString() {
        return Long.toString(this.businessId);
    }

    /** 
     * Set the internal business ID from an String
     * @param internal the new value
     * @deprecated Used only for flex remoting, because flex does not support 64 bit longs.
     */
    @Deprecated
    public void setInternalLongAsString(String internal) {
        this.businessId = Long.parseLong(internal);
    }

    /**
     * Creates a clone of this class.
     */
    @SuppressWarnings("unchecked")
    public BusinessId<T> clone() {
        try {
            BusinessId<T> clone = (BusinessId<T>) super.clone();
            clone.businessId = this.businessId;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("this should never happen", e);
        }
    }
    
    /**
     * Return the business id in an string representation.
     * @return the business id as string.
     */
    public String getAsString() {
        return Long.toString(this.businessId);
    }
    
    /**
     * Pare an business id from an string.
     * @param <T> the concrete type
     * @param bidString the string to parse
     * @return the business id
     * @throws NumberFormatException if the bidString is no valid long or is null.
     */
    public static <T> BusinessId<T> parse(String bidString) throws NumberFormatException {
        return new BusinessId<T>(Long.parseLong(bidString));        
    }

    @Override
    public int compareTo(BusinessId<T> other) {
        if (this.businessId < other.businessId) {
            return -1;
        } else {
            if (this.businessId == other.businessId) {
                return 0;
            } else {
                return 1;
            }
        }
    }
        
}
