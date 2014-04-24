package com.queomedia.persistence;

import java.util.Collection;
import java.util.List;

/**
 * The Interface GenericEntityDao.
 * 
 * @param <T> The concrete type where this DAO is for.
 * @author Engelmann
 */
public interface GenericEntityDao<T> {

    /**
     * Find by hibernate id.
     * 
     * @param id the id
     * @param lock the lock
     * 
     * @return the t
     */
    T findByHibernateId(Long id, boolean lock);

    /**
     * Find all entities of this type.
     * 
     * @return the list< t>
     */
    List<T> findAll();

    /**
     * Find by example.
     * 
     * @param exampleInstance the example instance
     * 
     * @return the list< t>
     */
    List<T> findByExample(T exampleInstance);

    /**
     * Find by example.
     * 
     * @param exampleInstance the example instance
     * @param excludeProperty the exclude property
     * 
     * @return the list< t>
     */
    List<T> findByExample(final T exampleInstance, final String[] excludeProperty);

    /**
     * Make entity persistent.
     * 
     * @param entity the entity
     * 
     * @return the t
     */
    T makePersistent(T entity);

    /**
     * Make all entities persistent.
     * 
     * @param entities the entities
     * 
     * @return the t
     */
    void makePersistent(Collection<? extends T> entities);

    /**
     * Make entity transient.
     * 
     * @param entity the entity
     */
    void makeTransient(T entity);
    
    /**
     * Make the entities transient.
     * 
     * @param entities the entities
     */
    void makeTransient(final Collection<? extends T> entities);

    /**
     * Make all transient.
     */
    void makeAllTransient();
    
    /**
     * Force this session to flush. Must be called at the end of a
     * unit of work, before commiting the transaction and closing the
     * session (depending on {@link #setFlushMode flush-mode},
     * {@link Transaction#commit()} calls this method).
     * <p/>
     * <i>Flushing</i> is the process of synchronizing the underlying persistent
     * store with persistable state held in memory.
     *
     * @throws HibernateException Indicates problems flushing the session or
     * talking to the database.
     */
    void flushSession();
    
    /**
     * Clear the session. 
     */
    void emptySession();       

    /**
     * Remove this instance from the session cache. Changes to the instance will
     * not be synchronized with the database. This operation cascades to associated
     * instances if the association is mapped with <tt>cascade="evict"</tt>.
     *
     * @param entity a persistent instance
     * @throws HibernateException
     */
    void evict(T entity);

    /**
     * Remove this instance from the session cache. Changes to the instance will
     * not be synchronized with the database. This operation cascades to associated
     * instances if the association is mapped with <tt>cascade="evict"</tt>.
     *
     * @param entities a list of persistent instances
     * @throws HibernateException
     */
    void evict(Collection<? extends T> entities);

    /**
     * Re-read the state of the given instance from the underlying database. It is
     * inadvisable to use this to implement long-running sessions that span many
     * business tasks. This method is, however, useful in certain special circumstances.
     * For example
     * <ul>
     * <li>where a database trigger alters the object state upon insert or update
     * <li>after executing direct SQL (eg. a mass update) in the same session
     * <li>after inserting a <tt>Blob</tt> or <tt>Clob</tt>
     * </ul>
     *
     * @param entity a persistent or detached instance
     */
    void refresh(T entity);

    /**
     * Re-read the state of the given instances from the underlying database. It is
     * inadvisable to use this to implement long-running sessions that span many
     * business tasks. This method is, however, useful in certain special circumstances.
     * For example
     * <ul>
     * <li>where a database trigger alters the object state upon insert or update
     * <li>after executing direct SQL (eg. a mass update) in the same session
     * <li>after inserting a <tt>Blob</tt> or <tt>Clob</tt>
     * </ul>
     *
     * @param entities a list of persistent or detached instances
     */
    void refresh(Collection<? extends T> entities);

    /**
     * Determine the number of entities stored in the database.
     * The result is the same like {@code SSELECT COUNT(*) FROM table}
     * @return the number of entities stored in the database.
     */
    int numberEntities();

    
    /**
     * Copy the state of the given object onto the persistent object with the same
     * identifier. If there is no persistent instance currently associated with
     * the session, it will be loaded. Return the persistent instance. If the
     * given instance is unsaved, save a copy of and return it as a newly persistent
     * instance. The given instance does not become associated with the session.
     * This operation cascades to associated instances if the association is mapped
     * with <tt>cascade="merge"</tt>.<br>
     * <br>
     * The semantics of this method are defined by JSR-220.
     *
     * @param object a detached instance with state to be copied
     * @return an updated persistent instance
     */
    T merge(T entity);
    


}
