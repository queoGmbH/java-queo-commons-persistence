package com.queomedia.persistence.hibernate.entitymanager;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import javax.persistence.PersistenceContext;

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
        return this.findByCriteria();
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

    /*
     * @see com.queomedia.fff.db.GenericDAO#findById(long, boolean)
     */
    @Override
    @SuppressWarnings("unchecked")
    public T findByHibernateId(final Long id, final boolean lock) {
        Check.notNullArgument(id, "id");

        if (lock) {            
            return (T) this.entityManager.getSession().load(this.getPersistentClass(), id, LockMode.PESSIMISTIC_WRITE);
        } else {
            return (T) this.entityManager.getSession().load(this.getPersistentClass(), id);
        }
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

    protected Criteria getCriteriaAPI() {
        return entityManager.getSession().createCriteria(this.persistentClass);
    }

    /**
     * Create and execute a criteria api call for the defined criterons on the specified class.
     * 
     * @param persistentClass the persistent class
     * @param criterion the criterions
     * 
     * @return the list< t>
     */
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
     * @param queryString the HQL statement
     * @return the matching items.
     */
    @SuppressWarnings("unchecked")
    protected List<T> findByHQL(final String queryString, final ResultTransformer resultTransformer) {
        Query query = entityManager.getSession().createQuery(queryString);
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

    @Override
    public int numberEntities() {
        Criteria crit = getCriteriaAPI().setProjection(Projections.rowCount());
        Integer count = (Integer) crit.uniqueResult();
        assert (count != null);

        return count;
    }

    @Override
    public T merge(final T entity) {
        return entityManager.merge(entity);
    }

    public javax.persistence.Query createHQLQuery(String query) {
        return entityManager.createQuery(query);
    }
}
