package com.queomedia.persistence.extra.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

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

    public static ObjectMapper configuredObjectMapper(GeneralLoaderDao generalLoaderDao) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(new SwitchingBusinessEntityModule(generalLoaderDao), new BusinessEntityOmitIdModule(),
                new BusinessIdModule());
        return mapper;
    }

    public ObjectMapper configuredObjectMapper() {
        return configuredObjectMapper(this.context.mock(GeneralLoaderDao.class));
    }

    @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.BUSINESS_ID)
    public static class DemoContainerClassBid {
        private String value1;

        private DemoBusinessEntity demoBusinessEntity;

        private String value2;

        public DemoContainerClassBid() {
            super();
        }

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

        public DemoContainerClassEntity() {
            super();
        }

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
     * @throws JException - no exception expected
     */
    @Test
    public void testSerializerForBusinessEntity() throws Exception {
        /* given: a Business Entity */
        BusinessId<DemoBusinessEntity> businessId = new BusinessId<>(123);
        DemoBusinessEntity businessEntity = new DemoBusinessEntity(businessId, "Hello World");

        /*
         * when: using jackson with the BussinessEntiyModule to serialize the Business
         * entity
         */
        String jsonResult = configuredObjectMapper().writeValueAsString(businessEntity);

        /* then: the returned json string is just the business id json-string */
        JSONAssert.assertEquals("{'businessId':'123', 'content':'Hello World'}", jsonResult,
                JSONCompareMode.NON_EXTENSIBLE);
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
        DemoBusinessEntity result = configuredObjectMapper(generalLoaderDao).readValue(jsonString,
                DemoBusinessEntity.class);

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
        final String jsonString = "{\"businessId\":\"1234\",\"content\":\"Hello World\"}";
        final BusinessId<DemoBusinessEntity> businessId = new BusinessId<>(1234);
        final DemoBusinessEntity businessEntity = new DemoBusinessEntity(businessId, "Hello World");

        /*
         * when: using jackson with the BussinessEntiyModule to deserialize the Business
         * entity
         */
        DemoBusinessEntity result = configuredObjectMapper().readValue(jsonString, DemoBusinessEntity.class);

        /* then: the json string is just the business id json-string */
        assertDeepEquals(businessEntity, result);
    }

    @Test
    public void testJson() throws JsonParseException, JsonMappingException, IOException {
        final String jsonString = "{\"businessId\":\"123\",\"content\":\"Hello World\"}";

        DemoBusinessEntity result = configuredObjectMapper().readValue(jsonString, DemoBusinessEntity.class);

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
        DemoContainerClassBid container = new DemoContainerClassBid("v1",
                new DemoBusinessEntity(new BusinessId<>(123), "Hello World"), "v2");

        String jsonResult = configuredObjectMapper().writeValueAsString(container);
        JSONAssert.assertEquals("{'value1':'v1', 'demoBusinessEntity':'123', 'value2':'v2'}", jsonResult,
                JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testDeserializeDemoContaintClassBid() throws Exception {
        final String jsonString = "{'value1':'v1', 'demoBusinessEntity':'123', 'value2':'v2'}".replace("'", "\"");

        DemoBusinessEntity businessEntity = new DemoBusinessEntity(new BusinessId<>(123), "Hello World");

        final GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class);
        this.context.checking(new Expectations() {
            {
                oneOf(generalLoaderDao).getByBusinessId(businessEntity.getBusinessId(),
                        SwitchingBusinessEntityModuleTest.DemoBusinessEntity.class);
                will(returnValue(businessEntity));
            }
        });

        DemoContainerClassBid result = configuredObjectMapper(generalLoaderDao).readValue(jsonString,
                DemoContainerClassBid.class);

        assertThat(result).usingRecursiveComparison().isEqualTo(new DemoContainerClassBid("v1", businessEntity, "v2"));
    }

    /**
     * Scenario: deserialize to a Business Entity.
     *
     * @throws Exception - no exception should not been thrown in this test case
     */
    @Test
    public void testSerializeDemoContainerClassEntity() throws Exception {
        DemoContainerClassEntity container = new DemoContainerClassEntity("v1",
                new DemoBusinessEntity(new BusinessId<>(123), "Hello World"), "v2");

        String jsonResult = configuredObjectMapper().writeValueAsString(container);

        JSONAssert.assertEquals(
                "{'value1':'v1', 'demoBusinessEntity':{'businessId':'123', 'content':'Hello World'},  'value2':'v2'}",
                jsonResult, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testDeserializeDemoContaintClassEntity() throws Exception {
        final String jsonString = "{'value1':'v1', 'demoBusinessEntity':{'businessId':'123', 'content':'Hello World'},  'value2':'v2'}"
                .replace("'", "\"");

        DemoContainerClassEntity result = configuredObjectMapper().readValue(jsonString,
                DemoContainerClassEntity.class);

        assertThat(result).usingRecursiveComparison().isEqualTo(
                new DemoContainerClassEntity("v1", new DemoBusinessEntity(new BusinessId<>(123), "Hello World"), "v2"));
    }
}
