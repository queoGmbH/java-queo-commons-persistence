package com.queomedia.persistence.extra.fake;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.queomedia.commons.exceptions.NotImplementedException;
import com.queomedia.persistence.BusinessEntity;

/**
 * An extension of {@link AbstractGenericDaoFake} that also provides the methods from {@link JpaSpecificationExecutor}.
 *
 * Because the methods from {@link JpaSpecificationExecutor} could not been simulated in an fake, this implementation
 * just throw an {@link com.queomedia.commons.exceptions.NotImplementedException}.
 *
 * @param <T> the business entity-
 *
 * @author engelmann
 */
public class AbstractGenericSpecificationExecutorDaoFake<T extends BusinessEntity<T>>
        extends AbstractGenericDaoFake<T> implements JpaSpecificationExecutor<T> {

    @Override
    public Optional<T> findOne(final Specification<T> spec) {
        throw new NotImplementedException();
    }

    @Override
    public List<T> findAll(final Specification<T> spec) {
        throw new NotImplementedException();
    }

    @Override
    public Page<T> findAll(final Specification<T> spec, final Pageable pageable) {
        throw new NotImplementedException();
    }

    @Override
    public List<T> findAll(final Specification<T> spec, final Sort sort) {
        throw new NotImplementedException();
    }

    @Override
    public long count(final Specification<T> spec) {
        throw new NotImplementedException();
    }

}
