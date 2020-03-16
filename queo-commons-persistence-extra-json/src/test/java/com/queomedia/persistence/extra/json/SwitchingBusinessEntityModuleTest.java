package com.queomedia.persistence.extra.json;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.extra.json.SwitchingBusinessEntityAnnotation.BusinessEntitySerialization;

public class SwitchingBusinessEntityModuleTest {

    /** JMock Context */
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    /**
     * A demo entity only used in this test. Attention: this class must not have
     * a @Entity annotation (in order to prevent Hibernate from creating a table for
     * it)
     */
    private static class DemoBusinessEntity extends BusinessEntity<DemoBusinessEntity> {

        /** Auto generated id for serializing. */
        private static final long serialVersionUID = 1292573538205615577L;

        private String content;

        public DemoBusinessEntity() {
            super();
        }

        /**
         * Simple, empty constructor.
         * 
         * @param businessId businessId of the new entity.
         */
        public DemoBusinessEntity(final BusinessId<DemoBusinessEntity> businessId, final String content) {
            super(businessId);
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    public static void assertDeepEquals(DemoBusinessEntity expected, DemoBusinessEntity actual) {
        assertEquals(expected, actual);
        assertEquals(expected.getContent(), actual.getContent());
    }

    public static ObjectMapper configureObjectMapper(GeneralLoaderDao generalLoaderDao) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(new SwitchingBusinessEntityModule(generalLoaderDao), new BusinessEntityOmitIdModule(),
                new BusinessIdModule());
        return mapper;
    }

    @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.BUSINESS_ID)
    public static class DemoContainerClassBid {
        private String value1;

        private DemoBusinessEntity demoBusinessEntity;

        private String value2;

        public DemoContainerClassBid(String value1, DemoBusinessEntity demoBusinessEntity, String value2) {
            this.value1 = value1;
            this.demoBusinessEntity = demoBusinessEntity;
            this.value2 = value2;
        }

        public String getValue1() {
            return value1;
        }

        public DemoBusinessEntity getDemoBusinessEntity() {
            return demoBusinessEntity;
        }

        public String getValue2() {
            return value2;
        }

    }

    @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.ENTITY)
    public static class DemoContainerClassEntity {
        private String value1;

        private DemoBusinessEntity demoBusinessEntity;

        private String value2;

        public DemoContainerClassEntity(String value1, DemoBusinessEntity demoBusinessEntity, String value2) {
            this.value1 = value1;
            this.demoBusinessEntity = demoBusinessEntity;
            this.value2 = value2;
        }

        public String getValue1() {
            return value1;
        }

        public DemoBusinessEntity getDemoBusinessEntity() {
            return demoBusinessEntity;
        }

        public String getValue2() {
            return value2;
        }
    }

    /**
     * Scenario: serialize a Business Entity.
     *
     * @throws JsonProcessingException - should not been thrown in this test case
     */
    @Test
    public void testSerializerForBusinessEntity() throws JsonProcessingException {
        /* given: a Business Entity */
        BusinessId<DemoBusinessEntity> businessId = new BusinessId<>(123);
        DemoBusinessEntity businessEntity = new DemoBusinessEntity(businessId, "Hello World");

        /*
         * when: using jackson with the BussinessEntiyModule to serialize the Business
         * entity
         */
        ObjectMapper mapper = configureObjectMapper(this.context.mock(GeneralLoaderDao.class));

        String jsonString = mapper.writeValueAsString(businessEntity);

        /* then: the returned json string is just the business id json-string */
        assertEquals("{\"businessId\":\"123\",\"content\":\"Hello World\"}", jsonString);
    }

    /**
     * Scenario: deserialize to a Business Entity.
     *
     * @throws Exception - no exception should not been thrown in this test case
     */
    @Test
    public void testDeserializerForBusinessEntity_bid() throws Exception {
        /* given: a json string that represent a business id and the business entity */
        final BusinessId<DemoBusinessEntity> businessId = new BusinessId<>(123);
        final String jsonString = '"' + businessId.getAsString() + '"';
        final DemoBusinessEntity businessEntity = new DemoBusinessEntity(businessId, "Hello World");

        /*
         * then: the deserializer use the GeneralLoaderDao to load the entity by its
         * business id
         */
        final GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class);
        this.context.checking(new Expectations() {
            {
                oneOf(generalLoaderDao).getByBusinessId(businessId,
                        SwitchingBusinessEntityModuleTest.DemoBusinessEntity.class);
                will(returnValue(businessEntity));
            }
        });

        /*
         * when: using jackson with the BussinessEntiyModule to deserialize the Business
         * entity
         */
        ObjectMapper mapper = configureObjectMapper(generalLoaderDao);

        DemoBusinessEntity result = mapper.readValue(jsonString, DemoBusinessEntity.class);

        /* then: the json string is just the business id json-string */
        assertDeepEquals(businessEntity, result);
    }

    /**
     * Scenario: deserialize to a Business Entity.
     *
     * @throws Exception - no exception should not been thrown in this test case
     */
    @Test
    public void testDeserializerForBusinessEntity_content() throws Exception {
        /* given: a json string that represent a business id and the business entity */
        final String jsonString = "{\"businessId\":\"123\",\"content\":\"Hello World\"}";
        final BusinessId<DemoBusinessEntity> businessId = new BusinessId<>(123);
        final DemoBusinessEntity businessEntity = new DemoBusinessEntity(businessId, "Hello World");

        /* then: no invocation expected */
        final GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class);

        /*
         * when: using jackson with the BussinessEntiyModule to deserialize the Business
         * entity
         */
        ObjectMapper mapper = configureObjectMapper(generalLoaderDao);

        DemoBusinessEntity result = mapper.readValue(jsonString, DemoBusinessEntity.class);

        /* then: the json string is just the business id json-string */
        assertDeepEquals(businessEntity, result);
    }

    @Test
    public void testJson() throws JsonParseException, JsonMappingException, IOException {
        final String jsonString = "{\"businessId\":\"123\",\"content\":\"Hello World\"}";
        
        ObjectMapper mapper = configureObjectMapper(this.context.mock(GeneralLoaderDao.class));
       
        DemoBusinessEntity result = mapper.readValue(jsonString, DemoBusinessEntity.class);

        /* then: the json string is just the business id json-string */
        final BusinessId<DemoBusinessEntity> businessId = new BusinessId<>(123);
        final DemoBusinessEntity businessEntity = new DemoBusinessEntity(businessId, "Hello World");
        assertDeepEquals(businessEntity, result);
    }

    /**
     * Scenario: deserialize to a Business Entity.
     *
     * @throws Exception - no exception should not been thrown in this test case
     */
    @Test
    public void testSerializeDemoContainerClassBid() throws Exception {

        DemoContainerClassBid container = new DemoContainerClassBid("value1",
                new DemoBusinessEntity(new BusinessId<>(123), "Hello Worls"), "value2");
        
        ObjectMapper mapper = configureObjectMapper(this.context.mock(GeneralLoaderDao.class));
        
        mapper.writeValueAsString(container); 

    }
}
