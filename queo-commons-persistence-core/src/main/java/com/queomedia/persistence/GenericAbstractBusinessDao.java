/**
 * 
 */
package com.queomedia.persistence;

import java.util.List;

import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.queomedia.commons.exceptions.NotFoundRuntimeException;

/**
 * The Interface GenericAbstractBusinessDao.
 * 
 * @param <KeyType> the Type where the Key is from
 * @param <T> the Business class where this DAO is for.
 * 
 * @author Engelmann
 */
public interface GenericAbstractBusinessDao<KeyType extends BusinessEntity<?>, T extends KeyType> extends GenericEntityDao<T> {

    /**
     * Get the business class by its business id.
     *
     * @param businessId the business id
     * @return the business class object
     * @throws NotFoundRuntimeException the business id is not found
     */
    T getByBusinessId(final BusinessId<KeyType> businessId)
            throws NotFoundRuntimeException;

    /**
     * Get the business classes by its business id's.
     *
     * @param businessId the business id
     * @return the list
     * @throws org.springframework.dao.EmptyResultDataAccessException the empty result data access exception
     * @throws org.springframework.dao.IncorrectResultSizeDataAccessException incorrect result size data access exception
     */
    List<T> getByBusinessId(final List<BusinessId<KeyType>> businessId)
            throws NotFoundRuntimeException;
    
    /**
     * Find the object by its business id.
     * 
     * @param businessId the business id
     * 
     * @return the business class object or null 
     * @throws IncorrectResultSizeDataAccessException if there are more than one object with the specified business id
     */
    T findByBusinessIdOrNull(final BusinessId<KeyType> businessId)
            throws IncorrectResultSizeDataAccessException;
}
