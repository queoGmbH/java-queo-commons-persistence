package com.queomedia.persistence.hibernate.entitymanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotFoundRuntimeException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.GenericAbstractBusinessDao;
import com.queomedia.persistence.util.ResultUtil;

/**
 * @param <KeyType> the Type where the Key is from
 * @param <T> the concrete type where this DAO is used for
 * @author Engelmann
 *
 */
public abstract class GenericHibernateAbstractBusinessDaoImpl<KeyType extends BusinessEntity<?>, T extends KeyType>
        extends GenericHibernateDaoImpl<T> implements GenericAbstractBusinessDao<KeyType, T> {
    /**
    * Logger for this class.
    */
    private static final Log LOGGER = LogFactory.getLog(GenericHibernateAbstractBusinessDaoImpl.class);

    @Override
    public T getByBusinessId(final BusinessId<KeyType> businessId) throws NotFoundRuntimeException {
        Check.notNullArgument(businessId, "businessId");

        return ResultUtil.requiredOneResult(findByCriteria(Restrictions.eq("businessId", businessId)), "businessId="
                + businessId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> getByBusinessId(final List<BusinessId<KeyType>> businessIds)
            throws EmptyResultDataAccessException, IncorrectResultSizeDataAccessException {
        Check.notNullArgument(businessIds, "businessIds");
        int bidCount = businessIds.size();

        /* shortcut if businessIds empty */
        if (bidCount == 0) {
            if (GenericHibernateAbstractBusinessDaoImpl.LOGGER.isDebugEnabled()) {
                GenericHibernateAbstractBusinessDaoImpl.LOGGER
                        .debug("findByBusinessId(List<BusinessId<KeyType>>) - serarch for a empty list of businessIds"); //$NON-NLS-1$
            }
            return new ArrayList<T>();
        }

        /* load */
        List<T> found = super.findByCriteria(Restrictions.in("businessId", businessIds));
        int foundSize = found.size();

        /* check size */
        if (foundSize == 0) {
            throw new EmptyResultDataAccessException(bidCount);
        }
        if (foundSize != bidCount) {
            throw new IncorrectResultSizeDataAccessException(bidCount, foundSize);
        }

        /* copy the founded objects in a map to access them via there key */
        Map<BusinessId<T>, T> map = new HashMap<BusinessId<T>, T>(foundSize);
        for (int i = 0; i < foundSize; i++) {
            T entity = found.get(i);
            map.put((BusinessId<T>) entity.getBusinessId(), entity);
        }

        List<T> result = new ArrayList<T>(found.size());

        /* bing the founded entities in the same order as the id's */
        for (int i = 0; i < bidCount; i++) {
            result.add(map.get(businessIds.get(i)));
        }

        return result;
    }

    @Override
    public T findByBusinessIdOrNull(final BusinessId<KeyType> businessId) throws IncorrectResultSizeDataAccessException {
        Check.notNullArgument(businessId, "businessId");

        List<T> entities = findByCriteria(Restrictions.eq("businessId", businessId));
        return ResultUtil.requiredOneOrNoResult(entities);
    }

}
