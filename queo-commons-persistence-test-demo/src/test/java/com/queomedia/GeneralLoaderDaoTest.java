package com.queomedia;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.queomedia.base.test.hibernate.manytoonelazy.CompositeEntity;
import com.queomedia.base.test.hibernate.manytoonelazy.CompositeEntityDao;
import com.queomedia.commons.asserts.AssertUtil;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.GeneralLoaderDao;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(SpringTestContext.APPLICATION)
public class GeneralLoaderDaoTest {

    @Autowired
    private GeneralLoaderDao generalLoaderDao;

    @Autowired
    private CompositeEntityDao compositeEntityDao;

    @Test
    public void testFindAll_empty() {
        AssertUtil.isEmpty(generalLoaderDao.findAll(CompositeEntity.class));
    }

    @Test
    public void testFindAll() {

        CompositeEntity compositeEntityA = new CompositeEntity(new BusinessId<CompositeEntity>(123));
        compositeEntityDao.makePersistent(compositeEntityA);
        CompositeEntity compositeEntityB = new CompositeEntity(new BusinessId<CompositeEntity>(456));
        compositeEntityDao.makePersistent(compositeEntityB);

        AssertUtil.containsExact(Arrays.asList(compositeEntityA, compositeEntityB),
                generalLoaderDao.findAll(CompositeEntity.class));
    }

    @Test
    public void testFindAll_Sorted() {

        CompositeEntity compositeEntityA = new CompositeEntity(new BusinessId<CompositeEntity>(123));
        compositeEntityDao.makePersistent(compositeEntityA);        
        CompositeEntity compositeEntityB = new CompositeEntity(new BusinessId<CompositeEntity>(456));
        compositeEntityDao.makePersistent(compositeEntityB);

        AssertUtil.sameOrder(Arrays.asList(compositeEntityA, compositeEntityB),
                generalLoaderDao.findAll(CompositeEntity.class, new Sort(Direction.ASC, "businessId")));
        
        AssertUtil.sameOrder(Arrays.asList(compositeEntityB, compositeEntityA),
                generalLoaderDao.findAll(CompositeEntity.class, new Sort(Direction.DESC, "businessId")));
    }

    @Test
    public void testFindAll_SortedNull() {

        CompositeEntity compositeEntityA = new CompositeEntity(new BusinessId<CompositeEntity>(123));
        compositeEntityDao.makePersistent(compositeEntityA);
        CompositeEntity compositeEntityB = new CompositeEntity(new BusinessId<CompositeEntity>(456));
        compositeEntityDao.makePersistent(compositeEntityB);

        AssertUtil.containsExact(Arrays.asList(compositeEntityA, compositeEntityB),
                generalLoaderDao.findAll(CompositeEntity.class, null));
    }
}
