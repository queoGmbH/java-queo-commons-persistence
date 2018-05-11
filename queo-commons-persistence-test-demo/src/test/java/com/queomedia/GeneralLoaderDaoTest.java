package com.queomedia;

import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
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
import com.queomedia.persistence.hibernate.entitymanager.GeneralHibernateLoaderDaoImpl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

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
        AssertUtil.isEmpty(this.generalLoaderDao.findAll(CompositeEntity.class));
    }

    @Test
    public void testFindAll() {

        CompositeEntity compositeEntityA = new CompositeEntity(new BusinessId<CompositeEntity>(123));
        this.compositeEntityDao.makePersistent(compositeEntityA);
        CompositeEntity compositeEntityB = new CompositeEntity(new BusinessId<CompositeEntity>(456));
        this.compositeEntityDao.makePersistent(compositeEntityB);

        AssertUtil.containsExact(Arrays.asList(compositeEntityA, compositeEntityB),
                this.generalLoaderDao.findAll(CompositeEntity.class));
    }

    @Test
    public void testFindAll_Sorted() {

        CompositeEntity compositeEntityA = new CompositeEntity(new BusinessId<CompositeEntity>(123));
        this.compositeEntityDao.makePersistent(compositeEntityA);
        CompositeEntity compositeEntityB = new CompositeEntity(new BusinessId<CompositeEntity>(456));
        this.compositeEntityDao.makePersistent(compositeEntityB);

        AssertUtil.sameOrder(Arrays.asList(compositeEntityA, compositeEntityB),
                this.generalLoaderDao.findAll(CompositeEntity.class, new Sort(Direction.ASC, "businessId")));

        AssertUtil.sameOrder(Arrays.asList(compositeEntityB, compositeEntityA),
                this.generalLoaderDao.findAll(CompositeEntity.class, new Sort(Direction.DESC, "businessId")));
    }

    @Test
    public void testFindAll_Unsorted() {

        CompositeEntity compositeEntityA = new CompositeEntity(new BusinessId<CompositeEntity>(123));
        this.compositeEntityDao.makePersistent(compositeEntityA);
        CompositeEntity compositeEntityB = new CompositeEntity(new BusinessId<CompositeEntity>(456));
        this.compositeEntityDao.makePersistent(compositeEntityB);

        List<CompositeEntity> result = this.generalLoaderDao.findAll(CompositeEntity.class, Sort.unsorted());

        AssertUtil.containsExact(Arrays.asList(compositeEntityA, compositeEntityB), result);
    }

    @Test
    public void testFindAll_SortedNull_deprecationWarning() {

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(GeneralHibernateLoaderDaoImpl.class);

        ListAppender<ILoggingEvent> testAppender = new ListAppender<>();
        logger.addAppender(testAppender);
        testAppender.start();

        try {
            CompositeEntity compositeEntity = new CompositeEntity(new BusinessId<CompositeEntity>(123));
            this.compositeEntityDao.makePersistent(compositeEntity);

            List<CompositeEntity> result = this.generalLoaderDao.findAll(CompositeEntity.class, null);

            AssertUtil.containsExact(Arrays.asList(compositeEntity), result);

        } finally {
            logger.detachAppender(testAppender);
        }

        /** There is an deprecation warning for using sort=null instead of sort=Sort.unsorted expected*/
        List<ILoggingEvent> warnLogs = testAppender.list.stream().filter(logEvent -> logEvent.getLevel() == Level.WARN)
                .collect(Collectors.toList());
        AssertUtil.hasSize(1, warnLogs);
        assertThat(warnLogs.get(0).getFormattedMessage(), Matchers.containsString("Sort=null is deprecated"));
    }
}
