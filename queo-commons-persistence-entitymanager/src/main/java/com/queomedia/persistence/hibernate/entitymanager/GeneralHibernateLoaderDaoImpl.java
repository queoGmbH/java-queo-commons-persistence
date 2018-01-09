package com.queomedia.persistence.hibernate.entitymanager;

import java.io.Serializable;
import java.util.List;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.jpa.HibernateEntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;

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
    public <T extends BusinessEntity<? extends Serializable>> T findObject(final BusinessId<T> businessId,
            final Class<T> entityClass) {
        return findByBusinessId(businessId, entityClass);
    }

    @Override
    @Deprecated
    public <Clazz extends BusinessEntity<? extends Serializable>> Clazz getObject(final BusinessId<Clazz> businessId,
            final Class<Clazz> entityClass)
            throws NotFoundRuntimeException {
        return getByBusinessId(businessId, entityClass);
    }

    @Override
    public <Clazz extends BusinessEntity<? extends Serializable>> Clazz getByBusinessId(
            final BusinessId<Clazz> businessId, final Class<Clazz> entityClass)
            throws NotFoundRuntimeException {
        Check.notNullArgument(businessId, "businessId");
        Check.notNullArgument(entityClass, "entityClass");

        String messageIfNotFound = "businessId=" + businessId + ", entityClass=" + entityClass.getName();
        return ResultUtil.requiredOneResult(loadEntitiesByBusinessId(businessId, entityClass), messageIfNotFound);
    }

    @Override
    public <Clazz extends BusinessEntity<? extends Serializable>> Clazz findByBusinessId(
            final BusinessId<Clazz> businessId, final Class<Clazz> entityClass) {
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
    protected <T> Criteria getCriteriaApi(final Class<T> entityClass) {
        return this.entityManager.getSession().createCriteria(entityClass);
    }

    /**
     * Gets the criteria builder.
     *
     * @return the criteria builder
     */
    protected CriteriaBuilder getCriteriaBuilder() {
        return this.entityManager.getCriteriaBuilder();
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
            final BusinessId<T> businessId, final Class<T> entityClass) {
        Criteria crit = getCriteriaApi(entityClass);
        crit.add(Restrictions.eq("businessId", businessId));

        @SuppressWarnings("unchecked")
        List<T> queryResult = crit.list();
        return queryResult;
    }

    @Override
    public <T> List<T> findAll(final Class<T> entityClass) {
        Check.notNullArgument(entityClass, "entityClass");

        return findAll(entityClass, null);
    }

    @Override
    public <T> List<T> findAll(final Class<T> entityClass, final Sort sort) {
        Check.notNullArgument(entityClass, "entityClass");
        //sort can be null - this is the same behavior like in spring data jpa repositories

        CriteriaBuilder builder = getCriteriaBuilder();

        CriteriaQuery<T> selectAllQuery = builder.createQuery(entityClass);
        Root<T> root = selectAllQuery.from(entityClass);
        selectAllQuery.select(root);

        if (sort != null) {
            selectAllQuery.orderBy(QueryUtils.toOrders(sort, root, builder));
        }

        return this.entityManager.createQuery(selectAllQuery).getResultList();
    }

}
