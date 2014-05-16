package com.queomedia.persistence.extra.springdatajpa.scenario;

import com.queomedia.persistence.extra.springdatajpa.BusinessEntityRepository;

/** We only need to test the methods from {@link BusinessEntityRepository}, so this interface is almost empty.*/
public interface SomeEntityRepository extends BusinessEntityRepository<SomeEntity, Long> {

}
