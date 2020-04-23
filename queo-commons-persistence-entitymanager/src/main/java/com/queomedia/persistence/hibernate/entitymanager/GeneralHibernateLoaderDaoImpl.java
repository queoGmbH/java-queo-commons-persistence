package com.queomedia.persistence.hibernate.entitymanager;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Criteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;

import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotFoundRuntimeException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.util.ResultUtil;

public class GeneralHibernateLoaderDaoImpl implements GeneralLoaderDao {

    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralHibernateLoaderDaoImpl.class);

    /** The entity manage injects by the container. */
    @PersistenceContext
    private EntityManager entityManager;

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
    @Deprecated
    protected <T> Criteria getCriteriaApi(final Class<T> entityClass) {
        return ((org.hibernate.ejb.HibernateEntityManager) this.entityManager).getSession().createCriteria(entityClass);
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

        CriteriaBuilder builder = getCriteriaBuilder();

        CriteriaQuery<T> selectByBidQuery = builder.createQuery(entityClass);
        Root<T> root = selectByBidQuery.from(entityClass);
        selectByBidQuery.where(builder.equal(root.get("businessId"), businessId));
        selectByBidQuery.select(root);

        return this.entityManager.createQuery(selectByBidQuery).getResultList();
    }

    @Override
    public <T> List<T> findAll(final Class<T> entityClass) {
        Check.notNullArgument(entityClass, "entityClass");

        return findAll(entityClass, Sort.unsorted());
    }

    @Override
    public <T> List<T> findAll(final Class<T> entityClass, final Sort sort) {
        Check.notNullArgument(entityClass, "entityClass");
        //sort can be null - but create a deprecation warning.

        if (sort == null) {
            LOGGER.warn("GeneralHibernateLoaderDaoImpl.findAll(Class, Sort) is invoked with sort=null. "
                    + "Sort=null is deprecated (treated as Sort.unsortable), use Sort.unsortable instead. "
                    + "This backwards compatibiliy id deprecread and will be removed in queo-commons-persistence 5.0. "
                    + "Invocation: entityClass=" + entityClass.getName() + "." + "\nfrom:\n"
                    + topStackTraceElements(Thread.currentThread().getStackTrace(), 1, 8));
        }

        CriteriaBuilder builder = getCriteriaBuilder();

        CriteriaQuery<T> selectAllQuery = builder.createQuery(entityClass);
        Root<T> root = selectAllQuery.from(entityClass);
        selectAllQuery.select(root);

        if (sort != null) { //remove this if-check and execute this statement always, as soon as it is required: sort  != null, 
            selectAllQuery.orderBy(QueryUtils.toOrders(sort, root, builder));
        }

        return this.entityManager.createQuery(selectAllQuery).getResultList();
    }

    /**
     * Return a String with the maxTopElements stack trace elements after skipTopElement.
     *
     * @param stackTrace - The result from {@code Thread.currentThread().getStackTrace()} The first element of the array represents the top of the stack, which is the most recent method invocation in the sequence.
     * @param skipTopElement the elements to skip before counting maxTopElements
     * @param maxTopElements the max top elements to return as string
     * @return the string
     */
    static String topStackTraceElements(final StackTraceElement[] stackTrace, int skipTopElement, int maxTopElements) {
        Check.notNullArgument(stackTrace, "stackTrace");
        Check.notNegativeArgument(skipTopElement, "skipTopElement");
        Check.notNegativeArgument(maxTopElements, "maxTopElements");

        // @formatter:off
        return Arrays.stream(stackTrace)
                .skip(skipTopElement)
                .limit(maxTopElements)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining(",\n"));        
        // @formatter:on
    }

}
