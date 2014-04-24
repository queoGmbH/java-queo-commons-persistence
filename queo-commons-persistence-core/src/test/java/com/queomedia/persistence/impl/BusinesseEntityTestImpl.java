package com.queomedia.persistence.impl;

import java.io.Serializable;

import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;

/**
 * The Class TestBusinessClass.
 */
public class BusinesseEntityTestImpl extends BusinessEntity<BusinesseEntityTestImpl> implements Serializable {

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = -1116505995236594202L;

    /**
     * The content.
     */
    private String content;

    /**
     * Constructor used by Hibernate only.
     * 
     * @deprecated This constructor must be only used by Hibernate.
     * It is not really depreciated, but this marker prevents programmers from using the constructor by mistake.
     */
    @Deprecated
    BusinesseEntityTestImpl() {
        super();
    }

    /**
     * Instantiates a new test business class.
     * 
     * @param content the content
     * @param businessId the business id
     */
    public BusinesseEntityTestImpl(final String content, final BusinessId<BusinesseEntityTestImpl> businessId) {
        super(businessId);
        this.content = content;
    }

    /**
     * Gets the content.
     * 
     * @return the content
     */
    public String getContent() {
        return this.content;
    }

    /**
     * Sets the content.
     * 
     * @param content the new content
     */
    public void setContent(final String content) {
        this.content = content;
    }

}
