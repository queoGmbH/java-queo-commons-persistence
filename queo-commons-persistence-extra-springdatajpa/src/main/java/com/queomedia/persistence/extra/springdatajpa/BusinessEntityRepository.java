package com.queomedia.persistence.extra.springdatajpa;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.queomedia.commons.exceptions.NotFoundRuntimeException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;

import de.humanfork.spring.data.jpa.nullaware.controll.NullResultMessage;

/**
 * {@link BusinessEntity} specific extension of {@link JpaRepository} that provide Repository functions that
 * are common for all {@link BusinessEntity}s. 
 *
 * @author Ralph Engelmann
 * @param <T> the generic type for the concrete {@link BusinessEntity}
 * @param <ID> the generic type the JPA primary Id type of the entity
 */
@NoRepositoryBean
public interface BusinessEntityRepository<T extends BusinessEntity<T>, ID extends Serializable> extends
        JpaRepository<T, ID> {

    /**
     * Loads a {@link BusinessEntity} of generic type T by the given {@link BusinessId}.
     * returns the found {@link BusinessEntity} or throws NotFoundRuntimeException.
     *
     * @param businessId the {@link BusinessId}.
     * @return the found {@link BusinessEntity} of type T
     * @throws NotFoundRuntimeException there is no {@link BusinessEntity} with that {@link BusinessId}
     */
    @NullResultMessage({ "wrong businessId {1}" })
    T getByBusinessId(BusinessId<T> businessId) throws NotFoundRuntimeException;

    /**
     * Loads a {@link BusinessEntity} of generic type T by the given {@link BusinessId}.
     * returns the found {@link BusinessEntity} or null.
     *
     * @param businessId the {@link BusinessId}.
     *
     * @return the found {@link BusinessEntity} or null
     */
    T findByBusinessId(BusinessId<T> businessId);

}
