package com.queomedia.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.StringTokenizer;

import org.junit.Before;
import org.junit.Test;

import com.queomedia.persistence.BusinessId;

public class PropertyEntityTest {

    private Entity entity;

    @Before
    public void setUp() {
        this.entity = new Entity();
    }

    @Test
    public void testGetSetProperty() {
        entity.setProperty("key", "value");

        assertEquals("value", entity.getProperty("key"));
    }

    @Test(expected = RuntimeException.class)
    public void testGetPropertyNotSet() {
        entity.getProperty("not existent");
    }

    @Test(expected = RuntimeException.class)
    public void testDoubleSet() {
        entity.setProperty("key", true);
        entity.setProperty("key", false);
    }

    /** Test the conventions method to derive the key name from the class name. */
    @Test
    public void testConventionSetProperty() {
        StringTokenizer value = new StringTokenizer("");

        entity.setProperty(value);
        assertEquals(value, entity.getProperty("stringTokenizer"));
    }

    @Test
    public void testGetProperties() {
        assertTrue(entity.getProperties().isEmpty());

        entity.setProperty("key", "value");
        assertEquals("value", entity.getProperties().get("key"));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testGetProperties_unmodifiable() {
        entity.getProperties().put("key", "value");

    }
    
    @Test
    public void testSetOrOverwriteProperty(){
        entity.setProperty("key", "value");
        entity.setOrOverwriteProperty("key", "value");
    }

    /** Simple class to subclass the property entity abstract class. */
    private static class Entity extends PropertyEntity<Long> {

        private static final long serialVersionUID = -5225043183966676138L;

        public Entity() {
            super(new BusinessId<Long>(1L));
        }
    }

}
