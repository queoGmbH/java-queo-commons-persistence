package com.queomedia;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
@ContextConfiguration(SpringTestContext.APPLICATION)
public class SpringContextSpringTest {

    @Resource
    private ApplicationContext applicationContext;

    @Test
    public void testSpringContextLoad() {
        Assert.assertNotNull("application context expected", this.applicationContext);
    }
}
