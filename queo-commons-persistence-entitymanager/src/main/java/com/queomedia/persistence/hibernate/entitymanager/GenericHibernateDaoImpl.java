package com.queomedia.persistence.hibernate.entitymanager;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import javax.persistence.LockModeType;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.Criteria;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.Projections;
import org.hibernate.jpa.HibernateEntityManager;
import org.hibernate.transform.ResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.query.QueryUtils;

import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.GenericEntityDao;

/**
 * Common base implementation for an DAO.
 * @param <T> The concrete type where this DAO is for.
 * @author Engelmann
 */
public class GenericHibernateDaoImpl<T> implements GenericEntityDao<T> {
    
    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericHibernateDaoImpl.class);
    

    /** The class type where this DAO is for. */
    private Class<T> persistentClass;

    /** The entity manage injects by the container. */
    @PersistenceContext
    private HibernateEntityManager entityManager;

    /**
     * Gets the entity manager.
     *
     * @return the entity manager
     */
    protected HibernateEntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * Instantiates a new generic hibernate dao impl.
     */
    @SuppressWarnings("unchecked")
    public GenericHibernateDaoImpl() {
        Type[] generics = ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments();

        if (generics.length == 1) {
            this.persistentClass = (Class<T>) generics[0];
        } else if (generics.length == 2) {
            this.persistentClass = (Class<T>) generics[1];
        } else {
            throw new RuntimeException("dont kown which type is the right");
        }
    }

    public Class<T> getPersistentClass() {
        return this.persistentClass;
    }

    /*
     * @see com.queomedia.fff.db.GenericDAO#findAll()
     */
    public List<T> findAll() {
        return this.findAll(null);
    }

    @Override
    public List<T> findAll(final Sort sort) {
        CriteriaBuilder builder = this.getCriteriaBuilder();

        CriteriaQuery<T> selectAllQuery = builder.createQuery(this.persistentClass);
        Root<T> root = selectAllQuery.from(this.persistentClass);
        selectAllQuery.select(root);

        if (sort != null) {
            selectAllQuery.orderBy(QueryUtils.toOrders(sort, root, builder));
        }

        return this.entityManager.createQuery(selectAllQuery).getResultList();
    }

    /*
     * @see com.queomedia.fff.db.GenericDAO#findByExample(java.lang.Object, java.langString[])
     */
    @SuppressWarnings("unchecked")
    public List<T> findByExample(final T exampleInstance, final String[] excludeProperty) {
        Check.notNullArgument(exampleInstance, "exampleInstance");
        Example example = Example.create(exampleInstance);
        for (String exclude : excludeProperty) {
            example.excludeProperty(exclude);
        }

        Criteria criteria = getCriteriaAPI();
        criteria.add(example);
        return criteria.list();
    }

    /*
     * @see com.queomedia.fff.db.GenericDAO#findByExample(java.lang.Object)
     */
    @Override
    public List<T> findByExample(final T exampleInstance) {
        return this.findByExample(exampleInstance, new String[0]);
    }
    
    
    /**
     * Find the entity by its primary key.
     *
     * @param id the id
     * @param lock the lock
     * @return the t
     */
    public T findByPrimaryKey(final Long id, final boolean lock) {
        Check.notNullArgument(id, "id");

        if (lock) {            
            return (T) this.entityManager.find(this.getPersistentClass(), id, LockModeType.PESSIMISTIC_WRITE);
        } else {
            return (T) this.entityManager.find(this.getPersistentClass(), id);
        }
    }

    /*
     * @see com.queomedia.fff.db.GenericDAO#findById(long, boolean)
     */
    @Override
    public T findByHibernateId(final Long id, final boolean lock) {
        Check.notNullArgument(id, "id");

        return findByPrimaryKey(id, lock);
    }

    @Override
    public T makePersistent(final T entity) {
        if (GenericHibernateDaoImpl.LOGGER.isDebugEnabled()) {
            GenericHibernateDaoImpl.LOGGER
                    .debug("makePersistent(T) - entity=" + entity + ", T=" + this.persistentClass.getSimpleName()); //$NON-NLS-1$
        }
        entityManager.getSession().saveOrUpdate(entity);
        return entity;
    }

    @Override
    public void makePersistent(final Collection<? extends T> entities) {
        Check.notNullArgument(entities, "entities");

        if (entities.size() == 0) {
            GenericHibernateDaoImpl.LOGGER
                    .warn("makePersistent(Collection<T>) - The collection of to save entities is empty. - T=" + this.persistentClass.getSimpleName()); //$NON-NLS-1$
            return;
        }

        for (T entity : entities) {
            entityManager.getSession().saveOrUpdate(entity);
        }
    }

    public void makeTransient(final T entity) {
        entityManager.getSession().delete(entity);
    }

    public void makeTransient(final Collection<? extends T> entities) {
        for (T entity : entities) {
            entityManager.getSession().delete(entity);
        }
    }

    public void makeAllTransient() {
        List<T> all = this.findAll();
        for (T item : all) {
            this.makeTransient(item);
        }
    }

    @Override
    public void flushSession() {
        entityManager.flush();
    }

    public void clear() {
        entityManager.clear();
    }
    
    /**
     * Gets the criteria builder.
     *
     * @return the criteria builder
     */
    protected CriteriaBuilder getCriteriaBuilder() {
        return this.entityManager.getCriteriaBuilder();
    }

    @Deprecated
    protected Criteria getCriteriaAPI() {
        return entityManager.getSession().createCriteria(this.persistentClass);
    }

    /**
     * Create and execute a criteria api call for the defined criterons on the specified class.
     * 
     * @param criterion the criterions
     * 
     * @return the list of matching entities
     */
    @Deprecated
    protected List<T> findByCriteria(final Criterion... criterion) {
        Criteria crit = getCriteriaAPI();
        for (Criterion c : criterion) {
            crit.add(c);
        }
        @SuppressWarnings("unchecked")
        List<T> result = (List<T>) crit.list(); 
        return result;
    }

    /**
     * Find objects with a HQL statement.
     * @param queryString the HQL statement
     * @return the matching items.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByHQL(final String queryString) {
        return entityManager.createQuery(queryString).getResultList();
    }

    /**
     * Find objects with a HQL statement.
     *
     * @param queryString the HQL statement
     * @param resultTransformer the result transformer
     * @return the matching entities.
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    protected List<T> findByHQL(final String queryString, final ResultTransformer resultTransformer) {
        Query query = entityManager.createQuery(queryString);
        query.setResultTransformer(resultTransformer);
        return query.list();
    }

    protected void executeNamedSkalarQuery(final String queryName) {
        entityManager.createNamedQuery(queryName).executeUpdate();
    }

    @Override
    public void emptySession() {
        Session session = entityManager.getSession();
        session.flush();
        session.clear();
    }

    @Override
    public void evict(final T entity) {
        entityManager.remove(entity);
    }

    @Override
    public void evict(final Collection<? extends T> entities) {
        for (T entity : entities) {
            entityManager.remove(entity);
        }
    }

    /*
     * @see com.queomedia.hibernate.GenericDAO#refresh(java.lang.Object)
     */
    @Override
    public void refresh(final T entity) {
        entityManager.refresh(entity);
    }

    @Override
    public void refresh(final Collection<? extends T> entities) {
        for (T entity : entities) {
            entityManager.refresh(entity);
        }
    }
    
    
    /**
     * Determine the number of entities stored in the database.
     * The result is the same like {@code SELECT COUNT(*) FROM table}
     * @return the number of entities stored in the database.
     */
    public long count() {
        CriteriaBuilder criteriaBuilder = this.getCriteriaBuilder();
        
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        countQuery.select(criteriaBuilder.count(countQuery.from(this.persistentClass)));
        
        return this.entityManager.createQuery(countQuery).getSingleResult();
    }

    /**
     * @deprecated use {@link #count()} instead.
     */
    @Deprecated
    @Override
    public int numberEntities() {
        return (int) count();
    }

    @Override
    public T merge(final T entity) {
        return entityManager.merge(entity);
    }

    public javax.persistence.Query createHQLQuery(String query) {
        return entityManager.createQuery(query);
    }
}
