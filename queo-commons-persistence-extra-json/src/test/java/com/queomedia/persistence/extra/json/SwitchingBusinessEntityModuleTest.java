package com.queomedia.persistence.extra.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.core.ConfigurableObjectInputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.queomedia.commons.checks.Check;
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

    /**
     * Build a Jackson Object Mappwer with {@link SwitchingBusinessEntityModule}
     * that use a {@link GeneralLoaderDao}-mock that expect exactly one invocation
     * of {@link GeneralLoaderDao#getByBusinessId(BusinessId, Class)} for the given
     * {@code entity}.
     * 
     * @param entity the entity that is returned by the
     *               {@link GeneralLoaderDao}-mock
     * @return a Jackson Object mapper with {@link SwitchingBusinessEntityModule}
     *         and mocked {@link GeneralLoaderDao}
     */
    @SuppressWarnings("unchecked")
    public <T extends BusinessEntity<? extends Serializable>> ObjectMapper configuredObjectMapper(
            final BusinessEntity<T> entity) {
        Check.notNullArgument(entity, "entity");

        GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class);
        this.context.checking(new Expectations() {
            {
                oneOf(generalLoaderDao).getByBusinessId(entity.getBusinessId(), (Class<T>) entity.getClass());
                will(returnValue(entity));
            }
        });

        return configuredObjectMapper(generalLoaderDao);
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

    public static class ListContainer {
        private List<DemoBusinessEntity> list;

        public ListContainer() {
            super();
        }

        public ListContainer(List<DemoBusinessEntity> list) {
            this.list = new ArrayList<>(list);
        }

        public List<DemoBusinessEntity> getList() {
            return list;
        }

        public void setList(List<DemoBusinessEntity> list) {
            this.list = list;
        }
    }

    @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.ENTITY)
    public static class GenericEntityWrapper<T> {
        private T content;

        public GenericEntityWrapper() {
            super();
        }

        public GenericEntityWrapper(T content) {
            this.content = content;
        }

        public T getContent() {
            return content;
        }

        public void setContent(T content) {
            this.content = content;
        }

    }

    @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.BUSINESS_ID)
    public static class GenericBusinessIdWrapper<T> {
        private T content;

        public GenericBusinessIdWrapper() {
            super();
        }

        public GenericBusinessIdWrapper(T content) {
            this.content = content;
        }

        public T getContent() {
            return content;
        }

        public void setContent(T content) {
            this.content = content;
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

    @Test
    public void testSerializeDemoBusinessEntityDemo_withEntityWrapper() throws Exception {
        DemoBusinessEntity content = new DemoBusinessEntity(new BusinessId<>(123), "Hello World");
        GenericEntityWrapper<DemoBusinessEntity> container = new GenericEntityWrapper<>(content);

        String jsonResult = configuredObjectMapper().writeValueAsString(container);

        JSONAssert.assertEquals("{'content':{'businessId':'123', 'content':'Hello World'}}", jsonResult,
                JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testDeserializeDemoBusinessEntityDemo_withEntityWrapper() throws Exception {

        final String jsonString = "{'content':{'businessId':'123', 'content':'Hello World'}}".replace("'", "\"");
        ObjectMapper jacksonMapper = configuredObjectMapper();

        GenericEntityWrapper<DemoBusinessEntity> result = jacksonMapper.readValue(jsonString,
                new TypeReference<GenericEntityWrapper<DemoBusinessEntity>>() {
                });

        DemoBusinessEntity expectedContent = new DemoBusinessEntity(new BusinessId<>(123), "Hello World");
        GenericEntityWrapper<DemoBusinessEntity> expectedContainer = new GenericEntityWrapper<>(expectedContent);
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedContainer);
    }

    @Test
    public void testSerializeDemoBusinessEntityDemo_withBidWrapper() throws Exception {
        DemoBusinessEntity content = new DemoBusinessEntity(new BusinessId<>(123), "Hello World");
        GenericBusinessIdWrapper<DemoBusinessEntity> container = new GenericBusinessIdWrapper<>(content);

        String jsonResult = configuredObjectMapper().writeValueAsString(container);

        JSONAssert.assertEquals("{'content':'123'}", jsonResult, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void testDeserializeDemoBusinessEntityDemo_withBidWrapper() throws Exception {

        final String jsonString = "{'content':'123'}".replace("'", "\"");

        DemoBusinessEntity businessEntity = new DemoBusinessEntity(new BusinessId<>(123), "Hello World");
        ObjectMapper jacksonMapper = configuredObjectMapper(businessEntity);
        GenericBusinessIdWrapper<DemoBusinessEntity> result = jacksonMapper.readValue(jsonString,
                new TypeReference<GenericBusinessIdWrapper<DemoBusinessEntity>>() {
                });
       
        
        GenericBusinessIdWrapper<DemoBusinessEntity> expectedContainer = new GenericBusinessIdWrapper<>(businessEntity);
        assertThat(result).usingRecursiveComparison().isEqualTo(expectedContainer);
    }

}
