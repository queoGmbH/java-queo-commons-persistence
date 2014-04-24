package com.queomedia.persistence.hibernate.entitymanager;

import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.GenericBusinessDao;

/**
 * @param <T> the concrete type where this DAO is used for
 * @author Engelmann
 *
 */
public abstract class GenericHibernateBusinessDaoImpl<T extends BusinessEntity<?>> extends
        GenericHibernateAbstractBusinessDaoImpl<T, T> implements GenericBusinessDao<T> {
}
