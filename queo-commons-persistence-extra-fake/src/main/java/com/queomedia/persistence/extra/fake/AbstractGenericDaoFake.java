package com.queomedia.persistence.extra.fake;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.ReflectionUtils;

import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotFoundRuntimeException;
import com.queomedia.commons.exceptions.NotImplementedCaseExecption;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;

/**
 * In memory DAO Fake implementation, usefull for tests.
 *
 * @author Ralph Engelmann
 *
 * @param <T> the entity type
 */
public class AbstractGenericDaoFake<T extends BusinessEntity<T>> implements JpaRepository<T, Long> {

    private long idCounter = 0;

    private final Set<T> savedEntities;

    public AbstractGenericDaoFake(final T... alreadyExistingTs) {
        this.savedEntities = new HashSet<T>();

        this.saveAll(Arrays.asList(alreadyExistingTs));
    }

    public Long getId(final T entity) {
        return entity.getId();
    }

    public boolean addToSavedEntities(final T entity) {
        return this.savedEntities.add(entity);
    }

    public boolean addAllToSavedEntities(final T... entities) {
        return this.savedEntities.addAll(Arrays.asList(entities));
    }

    public boolean removeFromSavedEntities(final T entity) {
        return this.savedEntities.remove(entity);
    }

    public long incAndGetIdCounter() {
        this.idCounter++;
        return this.idCounter;
    }

    Set<T> getSavedEntities() {
        return Collections.unmodifiableSet(this.savedEntities);
    }

    @Override
    public <S extends T> S save(final S entity) {
        setFieldByReflection(entity, "id", this.incAndGetIdCounter());

        this.addToSavedEntities(entity);
        return entity;
    }

    @Override
    public <S extends T> List<S> saveAll(final Iterable<S> entities) {
        ArrayList<S> result = new ArrayList<S>();

        for (S entity : entities) {
            result.add(this.save(entity));
        }

        return result;
    }

    @Override
    public <S extends T> S saveAndFlush(final S entity) {
        return this.save(entity);
    }

    @Override
    public T getOne(final Long primaryKey) {
        return this.findById(primaryKey).orElseThrow(() -> new RuntimeException("not found"));
    }

    @Override
    public boolean existsById(final Long primaryKey) {
        return this.findById(primaryKey).isPresent();
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<T>(this.getSavedEntities());
    }

    //Suppress unchecked is ok, because the BeanComparator works on every type
    @SuppressWarnings("unchecked")
    protected Comparator<T> buildComparatorForSorting(final Sort sort) {
        Order firstOrder = sort.iterator().next();

        @SuppressWarnings("rawtypes")
        BeanComparator beanComparator = new BeanComparator(firstOrder.getProperty());
        if (firstOrder.isAscending()) {
            return beanComparator;
        } else {
            return new ReverseComparator(beanComparator);
        }
    }

    @Override
    public List<T> findAll(final Sort sort) {
        List<T> list = this.findAll();
        Collections.sort(list, buildComparatorForSorting(sort));
        return list;
    }

    @Override
    public Page<T> findAll(final Pageable pageable) {
        throw new NotImplementedException();
    }

    @Override
    public long count() {
        return this.getSavedEntities().size();
    }

    @Override
    public void delete(final T entity) {
        this.removeFromSavedEntities(entity);

    }

    @Override
    public void deleteAll(final Iterable<? extends T> entities) {
        for (T entity : entities) {
            this.delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        this.deleteAll(this.findAll());
    }

    @Override
    public void flush() {
        //nothing to do
    }

    public T getByBusinessId(final BusinessId<T> businessId) throws NotFoundRuntimeException {
        T entity = findByBusinessId(businessId);
        if (entity != null) {
            return entity;
        } else {
            throw new NotFoundRuntimeException();
        }
    }

    public T findByBusinessId(final BusinessId<T> businessId) {
        for (T entitiy : this.savedEntities) {
            if (entitiy.getBusinessId().equals(businessId)) {
                return entitiy;
            }
        }
        return null;
    }

    @Override
    public Optional<T> findById(final Long primaryKey) {
        for (T entity : this.getSavedEntities()) {
            if (getId(entity).equals(primaryKey)) {
                return Optional.of(entity);
            }
        }
        return Optional.empty();
    }

    @Override
    public void deleteById(final Long id) {
        this.delete(this.getOne(id));
    }

    @Override
    public List<T> findAllById(final Iterable<Long> primaryKeys) {
        List<T> result = new ArrayList<T>();

        for (Long primaryKey : primaryKeys) {
            findById(primaryKey).ifPresent(result::add);
        }
        return result;
    }

    @Override
    public void deleteInBatch(final Iterable<T> entities) {
        this.deleteAll(entities);
    }

    @Override
    public void deleteAllInBatch() {
        this.deleteAll();
    }

    /**
     * Set the {@link Field field} with the given {@code name} on the provided
     * {@link Object target object} to the supplied {@code value}.
     *
     *
     * @param target the target object on which to set the field
     * @param name the name of the field to set
     * @param value the value to set
     * @see ReflectionUtils#findField(Class, String, Class)
     * @see ReflectionUtils#makeAccessible(Field)
     * @see ReflectionUtils#setField(Field, Object, Object)
     */
    private static void setFieldByReflection(final Object target, final String name, final Object value) {
        Check.notNullArgument(target, "target");
        Check.notNullArgument(name, "name");
        //value can be null of course

        Field field = ReflectionUtils.findField(target.getClass(), name);

        if (field == null) {
            throw new IllegalArgumentException(String.format("Could not find field [%s] on target [%s]", name, target));
        }

        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, target, value);
    }

    @Deprecated
    @Override
    public <S extends T> Optional<S> findOne(final Example<S> example) {
        throw new NotImplementedCaseExecption();
    }

    @Deprecated
    @Override
    public <S extends T> Page<S> findAll(final Example<S> example, final Pageable pageable) {
        throw new NotImplementedCaseExecption();
    }

    @Deprecated
    @Override
    public <S extends T> long count(final Example<S> example) {
        throw new NotImplementedCaseExecption();
    }

    @Deprecated
    @Override
    public <S extends T> boolean exists(final Example<S> example) {
        throw new NotImplementedCaseExecption();
    }

    @Deprecated
    @Override
    public <S extends T> List<S> findAll(final Example<S> example) {
        throw new NotImplementedCaseExecption();
    }

    @Deprecated
    @Override
    public <S extends T> List<S> findAll(final Example<S> example, final Sort sort) {
        throw new NotImplementedCaseExecption();
    }
}
