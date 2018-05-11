package com.queomedia.persistence;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Sort;

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
    
    
    /**
     * Load the entity of an specific type.
     *
     * @param <Clazz> the class of the business object
     * @param entityClass the entity class
     * @return the all business entities
     */
    <Clazz> List<Clazz> findAll(Class<Clazz> entityClass);

    /**
     * Load the entity of an specific type and sort them.
     *
     * @param <Clazz> the class of the business object
     * @param entityClass the entity class
     * @param sort the used sorting, or null - this was the same behavior in spring data jpa PagingAndSortingRepository
     *        before 2.0 (Releas Train Kay). We keep this behavior for backward compatibility, but in next major release
     *        of queo-commons-persistence (5.0) this parameter is not longer allowed to be null, instead use {@link Sort#unsorted()} 
     * @return the all business entities sorted
     */
    <Clazz> List<Clazz> findAll(Class<Clazz> entityClass, Sort sort);
    
}
