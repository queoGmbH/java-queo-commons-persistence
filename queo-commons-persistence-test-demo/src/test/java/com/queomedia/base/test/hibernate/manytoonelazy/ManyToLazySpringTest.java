package com.queomedia.base.test.hibernate.manytoonelazy;

import static org.junit.Assert.assertThat;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.queomedia.SpringTestContext;
import com.queomedia.persistence.BusinessId;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(SpringTestContext.APPLICATION)
public class ManyToLazySpringTest {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Test
    public void testLoadLazy() {
        BusinessId<CompositeEntity> compositeBid = new BusinessId<CompositeEntity>(123);
        CompositeEntity composite = new CompositeEntity(compositeBid);
        entityManager.persist(composite);
        
        
        BusinessId<ComponentEntity> componentBid = new BusinessId<ComponentEntity>(456);
        ComponentEntity component = new ComponentEntity(composite, componentBid);
        entityManager.persist(component);
        
        entityManager.flush();
        entityManager.clear();
        
        TypedQuery<ComponentEntity> query = this.entityManager.createQuery("SELECT c FROM ComponentEntity c WHERE c.businessId=:bid", ComponentEntity.class).setParameter("bid", componentBid);
        List<ComponentEntity> resultList = query.getResultList();
        
        assertThat(resultList, Matchers.hasSize(1));
        assertThat(resultList, Matchers.hasItem(component));
    }
}
