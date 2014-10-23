package com.queomedia.persistence.hibernate.entitymanager;

import java.io.Serializable;
import java.util.List;

import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jpa.HibernateEntityManager;

import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotFoundRuntimeException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.util.ResultUtil;

public class GeneralHibernateLoaderDaoImpl implements GeneralLoaderDao {

    
    /** The entity manage injects by the container. */
    @PersistenceContext
    private HibernateEntityManager entityManager;

    @Override
    @Deprecated
    public <T extends BusinessEntity<? extends Serializable>> T findObject(BusinessId<T> businessId,
            Class<T> entityClass) {
       return findByBusinessId(businessId, entityClass);
    }

    @Override
    @Deprecated
    public <Clazz extends BusinessEntity<? extends Serializable>> Clazz getObject(BusinessId<Clazz> businessId,
            Class<Clazz> entityClass) throws NotFoundRuntimeException {
        return getByBusinessId(businessId, entityClass);
    }
    
    @Override
    public <Clazz extends BusinessEntity<? extends Serializable>> Clazz getByBusinessId(BusinessId<Clazz> businessId,
            Class<Clazz> entityClass) throws NotFoundRuntimeException {
        Check.notNullArgument(businessId, "businessId");
        Check.notNullArgument(entityClass, "entityClass");

        String messageIfNotFound = "businessId=" + businessId + ", entityClass=" + entityClass.getName();
        return ResultUtil.requiredOneResult(loadEntitiesByBusinessId(businessId, entityClass), messageIfNotFound);
    }

    @Override
    public <Clazz extends BusinessEntity<? extends Serializable>> Clazz findByBusinessId(BusinessId<Clazz> businessId,
            Class<Clazz> entityClass) {
        Check.notNullArgument(businessId, "businessId");
        Check.notNullArgument(entityClass, "entityClass");

        return ResultUtil.requiredOneOrNoResult(loadEntitiesByBusinessId(businessId, entityClass));
    }

    /**
     * Gets the criteria api.
     *
     * @param <T> the generic type
     * @param entityClass the entity class
     * @return the criteria api
     */
    protected <T> Criteria getCriteriaApi(Class<T> entityClass) {
        return entityManager.getSession().createCriteria(entityClass);
    }
    
    /**
     * Load all entities of the class by its business id.
     * The result is not checked!
     *
     * @param <T> the generic type
     * @param businessId the business id
     * @param entityClass the entity class
     * @return the list of entities
     */
    private <T extends BusinessEntity<? extends Serializable>> List<T> loadEntitiesByBusinessId(
            BusinessId<T> businessId, Class<T> entityClass) {
        Criteria crit = getCriteriaApi(entityClass);
        crit.add(Restrictions.eq("businessId", businessId));

        @SuppressWarnings("unchecked")
        List<T> queryResult = (List<T>) crit.list();
        return queryResult;
    }   
}
