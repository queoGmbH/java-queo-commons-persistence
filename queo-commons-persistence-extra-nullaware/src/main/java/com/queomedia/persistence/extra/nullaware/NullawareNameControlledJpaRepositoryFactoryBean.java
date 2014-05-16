package com.queomedia.persistence.extra.nullaware;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;

import com.queomedia.commons.exceptions.NotFoundRuntimeException;

import de.humanfork.spring.data.jpa.nullaware.NullawareJpaRepositoryFactoryBean;
import de.humanfork.spring.data.jpa.nullaware.controll.NameControlledNullResultActionFactory;

/**
 * A Spring-Data-Jpa {@link JpaRepositoryFactoryBean} that apply the Nullware support configured to throw a
 * {@link NotFoundRuntimeException} by default.
 */
public class NullawareNameControlledJpaRepositoryFactoryBean<T extends JpaRepository<Object, Serializable>> extends
        NullawareJpaRepositoryFactoryBean<T> {

    private static final NameControlledNullResultActionFactory nameControlledNullResultActionFactory = new NameControlledNullResultActionFactory(NotFoundRuntimeException.class);

    public NullawareNameControlledJpaRepositoryFactoryBean() {
        super(nameControlledNullResultActionFactory);
    }

}
