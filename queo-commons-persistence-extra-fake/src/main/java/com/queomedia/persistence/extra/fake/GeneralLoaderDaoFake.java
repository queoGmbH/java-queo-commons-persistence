package com.queomedia.persistence.extra.fake;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Sort;

import com.queomedia.commons.exceptions.NotFoundRuntimeException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.GeneralLoaderDao;

/**
 * This is a simple fake implementation for an {@link GeneralLoaderDao}.
 *
 * The {@code get}-methods throw a {@link NotFoundRuntimeException}, while {@code find}-methods will either return null,
 * or an empty collection.
 *
 * @author gander
 */
public class GeneralLoaderDaoFake implements GeneralLoaderDao {

    @Override
    public <Clazz extends BusinessEntity<? extends Serializable>> Clazz getObject(final BusinessId<Clazz> businessId,
            final Class<Clazz> entityClass)
            throws NotFoundRuntimeException {
        throw new NotFoundRuntimeException();
    }

    @Override
    public <Clazz extends BusinessEntity<? extends Serializable>> Clazz findObject(final BusinessId<Clazz> businessId,
            final Class<Clazz> entityClass) {
        return null;
    }

    @Override
    public <Clazz extends BusinessEntity<? extends Serializable>> Clazz getByBusinessId(
            final BusinessId<Clazz> businessId, final Class<Clazz> entityClass)
            throws NotFoundRuntimeException {
        throw new NotFoundRuntimeException();
    }

    @Override
    public <Clazz extends BusinessEntity<? extends Serializable>> Clazz findByBusinessId(
            final BusinessId<Clazz> businessId, final Class<Clazz> entityClass) {
        return null;
    }

    @Override
    public <Clazz> List<Clazz> findAll(final Class<Clazz> entityClass) {
        return new ArrayList<>();
    }

    @Override
    public <Clazz> List<Clazz> findAll(final Class<Clazz> entityClass, final Sort sort) {
        return new ArrayList<>();
    }

}
