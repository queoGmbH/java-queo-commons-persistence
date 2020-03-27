package com.queomedia.persistence.extra.json.switchingbusinessentity;

import org.hibernate.boot.model.relational.Database;

import com.queomedia.persistence.BusinessEntity;

/**
 * The two modes how an {@link BusinessEntity} can be serialized and deserialized.
 */
public enum BusinessEntitySerializationMode {

    /**
     *  Serialize {@link BusinessEntity}s to there Business id,
     *  deserialize {@link BusinessEntity}s by loading them from {@link Database}.
     */
    BUSINESS_ID,

    /** Serialize and deserialize {@link BusinessEntity} with all attributes to/from json. */
    ENTITY;

}
