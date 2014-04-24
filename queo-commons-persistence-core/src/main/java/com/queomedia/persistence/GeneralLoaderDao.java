package com.queomedia.persistence;

import java.io.Serializable;

import com.queomedia.commons.exceptions.NotFoundRuntimeException;

/**
 * This dao provides less type save but more general access to load entities.
 */
public interface GeneralLoaderDao {

    /**
     * Load the Business entity of an specific type by its business id.
     *
     * @param <Clazz> the concrete business type
     * @param businessId the business id
     * @param entityClass the class of the business object
     * @return the business object
     * @throws NotFoundRuntimeException if there is no entity with this business id.
     * @deprecated use {@link #getByBusinessId(BusinessId, Class)}
     */
    @Deprecated
    <Clazz extends BusinessEntity<? extends Serializable>> Clazz getObject(BusinessId<Clazz> businessId,
            Class<Clazz> entityClass) throws NotFoundRuntimeException;
    
    /**
     * Load the Business entity of an specific type by its business id.
     * @param <Clazz> the concrete business type
     * @param businessId the business id
     * @param entityClass the class of the business object
     * @return the business object or null
     * @deprecated use {@link #findByBusinessId(BusinessId, Class)}
     */
    @Deprecated
    <Clazz extends BusinessEntity<? extends Serializable>> Clazz findObject(BusinessId<Clazz> businessId,
            Class<Clazz> entityClass);
    
    /**
     * Load the Business entity of an specific type by its business id.
     *
     * @param <Clazz> the concrete business type
     * @param businessId the business id
     * @param entityClass the class of the business object
     * @return the business object
     * @throws NotFoundRuntimeException if there is no entity with this business id.
     */
    <Clazz extends BusinessEntity<? extends Serializable>> Clazz getByBusinessId(BusinessId<Clazz> businessId,
            Class<Clazz> entityClass) throws NotFoundRuntimeException;
    
    /**
     * Load the Business entity of an specific type by its business id.
     * 
     * @param <Clazz> the concrete business type
     * @param businessId the business id
     * @param entityClass the class of the business object
     * @return the business object or null
     */
    <Clazz extends BusinessEntity<? extends Serializable>> Clazz findByBusinessId(BusinessId<Clazz> businessId,
            Class<Clazz> entityClass);
}
