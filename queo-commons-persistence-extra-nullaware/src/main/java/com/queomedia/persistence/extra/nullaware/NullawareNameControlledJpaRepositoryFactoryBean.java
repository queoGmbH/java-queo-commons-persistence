package com.queomedia.persistence.extra.nullaware;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

import de.humanfork.spring.data.jpa.nullaware.NullawareJpaRepositoryFactoryBean;
import de.humanfork.spring.data.jpa.nullaware.controll.NameControlledNullResultActionFactory;

public class NullawareNameControlledJpaRepositoryFactoryBean<T extends JpaRepository<Object, Serializable>> extends
        NullawareJpaRepositoryFactoryBean<T> {

    private static final NameControlledNullResultActionFactory nameControlledNullResultActionFactory = new NameControlledNullResultActionFactory(RuntimeException.class);

    public NullawareNameControlledJpaRepositoryFactoryBean() {
        super(nameControlledNullResultActionFactory);
    }

}
