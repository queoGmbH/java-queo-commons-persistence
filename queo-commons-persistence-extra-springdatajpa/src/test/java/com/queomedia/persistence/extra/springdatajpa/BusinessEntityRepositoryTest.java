package com.queomedia.persistence.extra.springdatajpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.springframework.transaction.annotation.Transactional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.queomedia.commons.exceptions.NotFoundRuntimeException;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.BusinessIdGenerator;
import com.queomedia.persistence.extra.springdatajpa.scenario.PersistenceTestContext;
import com.queomedia.persistence.extra.springdatajpa.scenario.SomeEntity;
import com.queomedia.persistence.extra.springdatajpa.scenario.SomeEntityRepository;

@SuppressWarnings("deprecation")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { PersistenceTestContext.class })
@Transactional
@TransactionConfiguration(defaultRollback = true)

/** Scenario: Test that one can extend the own repository from BusinessEntityRepository and the methods can used like normal. */
public class BusinessEntityRepositoryTest {

    @Autowired
    private SomeEntityRepository someEntityRepository;

    @Autowired
    private BusinessIdGenerator businessIdGenerator;

    /** Scenario: load an someEntity by its business id*/
    @Test
    public void testGetByBusinessId() {
        /** given: a persisted entity. */
        SomeEntity someEntity = this.createPersistedEntity();

        /** Then one can load this someEntity by its business id. */
        assertEquals(someEntity, this.someEntityRepository.getByBusinessId(someEntity.getBusinessId()));
    }

    /**
     * Scenario: - see overall scenario at {@link BusinessEntityRepositoryTest} -
     * try to get {@link SomeEntity} by its id, that does not exist, results in a {@link NotFoundRuntimeException}.
     * Requires that NULL-AWARE is enabled!
     */
    @Test(expected = NotFoundRuntimeException.class)
    public void testGetByBusinessId_withNotExistingBid() {
        /** given: no someEntity at all. */

        /** when: try to load an someEntity with an not existing business id. */
        this.someEntityRepository.getByBusinessId(new BusinessId<SomeEntity>(123));

        /** then: exception */
    }

    /**
     * Scenario:  - see overall scenario at {@link BusinessEntityRepositoryTest} -
     * load an someEntity by its business id
     */
    @Test
    public void testFindByBusinessId() {
        /** given: a persisted someEntity. */
        SomeEntity someEntity = this.createPersistedEntity();

        /** Then one can load this someEntity by its business id. */
        assertEquals(someEntity, this.someEntityRepository.findByBusinessId(someEntity.getBusinessId()));
    }

    /**
     * Scenario:
     *  - see overall scenario at {@link BusinessEntityRepositoryTest} -
     * load an someEntity by its business id
     */
    @Test
    public void testFindByBusinessId_withNotExistingBid() {
        /** given: no someEntity at all. */

        /** when: try to load an someEntity with an not existing business id. */
        SomeEntity result = this.someEntityRepository.findByBusinessId(new BusinessId<SomeEntity>(123));

        /** then the find query returns null. */
        assertNull(result);
    }

    /** Helper to create a persited {@link SomeEntity}. */
    private SomeEntity createPersistedEntity() {
        SomeEntity someEntity = new SomeEntity(this.businessIdGenerator.<SomeEntity> generateBusinessId());
        return this.someEntityRepository.save(someEntity);
    }

}
