package com.queomedia.persistence.util;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import com.queomedia.commons.exceptions.NotFoundRuntimeException;

public class ResultUtilTest {

    @Test
    public void testRequiredOneOrNoResult() {

        Object entity = new Object();
        List<Object> oneElementCollection = Arrays.asList(entity);

        Object result = ResultUtil.requiredOneOrNoResult(oneElementCollection);

        assertSame(entity, result);
    }

    @Test
    public void testRequiredOneOrNoResultEmpty() {

        List<Object> emptyElementCollection = Arrays.asList();

        Object result = ResultUtil.requiredOneOrNoResult(emptyElementCollection);

        assertNull(result);
    }

    @Test(expected = IncorrectResultSizeDataAccessException.class)
    public void testRequiredOneOrNoResultToMuch() {

        List<Object> emptyElementCollection = Arrays.asList(new Object(), new Object());
        ResultUtil.requiredOneOrNoResult(emptyElementCollection);
    }

    @Test
    public void testRequiredOneResult() {

        Object entity = new Object();
        List<Object> oneElementCollection = Arrays.asList(entity);

        Object result = ResultUtil.requiredOneResult(oneElementCollection, "");

        assertSame(entity, result);
    }
    
    @Test(expected=NotFoundRuntimeException.class)
    public void testRequiredOneResultEmpty() {

        List<Object> emptyElementCollection = Arrays.asList();

        ResultUtil.requiredOneResult(emptyElementCollection, "");
    }
    
    @Test(expected = IncorrectResultSizeDataAccessException.class)
    public void testRequiredOneResultToMuch() {

        List<Object> emptyElementCollection = Arrays.asList(new Object(), new Object());
        ResultUtil.requiredOneResult(emptyElementCollection, "");
    }

}
