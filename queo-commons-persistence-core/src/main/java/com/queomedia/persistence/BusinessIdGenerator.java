package com.queomedia.persistence;

public interface BusinessIdGenerator {

    /**
     * Create an new business id of type {@code<T>}
     * @param <T> the business class type
     * @return a new business id
     */
    <T extends BusinessEntity<T>> BusinessId<T> generateBusinessId();
}
