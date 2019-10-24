package com.queomedia.persistence.extra.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.queomedia.persistence.BusinessEntity;

/**
 * Jackson {@link com.fasterxml.jackson.databind.Module} that registers a mixin to exclude the fields
 * id, hibernateId, businessIdValue and isNew from business BusinessEntitys.
 *
 */
public class BusinessEntityOmitIdModule extends SimpleModule {

    private static final long serialVersionUID = 7475335424514392671L;

    /**
     * Instantiates a new business id module and register the mapping.
     */
    public BusinessEntityOmitIdModule() {
        super("businessEntityOmitIdModule", new Version(1, 0, 0, null, "com.queomedia", null));

        setMixInAnnotation(BusinessEntity.class, BusinessEntityJacksonMixin.class);
    }

    /**
     * Mixin for class {@link BusinessEntity}.
     * @author engelmann
     */
    static abstract class BusinessEntityJacksonMixin {

        /** Ignore {@link BusinessEntity#getId()}. */
        @JsonIgnore
        abstract Long getId();

        /** Ignore {@link BusinessEntity#getHibernateId()}. */
        @JsonIgnore
        abstract Long getHibernateId();

        /** Ignore {@link BusinessEntity#getBusinessIdValue}. */
        @JsonIgnore
        abstract public long getBusinessIdValue();

        /** Ignore {@link BusinessEntity#isNew()}. */
        @JsonIgnore
        abstract boolean isNew();
    }
}
