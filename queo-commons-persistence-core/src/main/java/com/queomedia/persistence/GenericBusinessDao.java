package com.queomedia.persistence;


/**
 * DAO Interface for classes where the class has its own dedicated business key type.
 * @author Engelmann
 * @param <T> the Business class where this DAO is for.
 */
public interface GenericBusinessDao<T extends BusinessEntity<?>> extends GenericAbstractBusinessDao<T, T> {

}
