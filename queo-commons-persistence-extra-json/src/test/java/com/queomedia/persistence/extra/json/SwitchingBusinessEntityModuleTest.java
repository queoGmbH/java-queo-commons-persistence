package com.queomedia.persistence.extra.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONException;
import org.junit.Rule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.queomedia.commons.checks.Check;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.GeneralLoaderDao;
import com.queomedia.persistence.extra.json.SwitchingBusinessEntityAnnotation.BusinessEntitySerialization;

//TODO Tests with Wrappers that use @JsonUnwarp
//TODO Test null
//TODO Test Optional
//TODO Test Map - key, value 
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

        @Override
        public String toString() {
            return "DemoBusinessEntity [content=" + content + "]";
        }

    }

    public static ObjectMapper configuredObjectMapper(GeneralLoaderDao generalLoaderDao) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(new SwitchingBusinessEntityModule(generalLoaderDao), new BusinessEntityOmitIdModule(),
                new BusinessIdModule());
        return mapper;
    }

    private int generalLoaderDaoMockCounter = 0;

    /**
     * Build a Jackson Object Mappwer with {@link SwitchingBusinessEntityModule}
     * that use a {@link GeneralLoaderDao}-mock that expect exactly one invocation
     * of {@link GeneralLoaderDao#getByBusinessId(BusinessId, Class)} for each given
     * entity (from {@code entities}).
     * 
     * @param entities the entities that is returned by the
     *                 {@link GeneralLoaderDao}-mock
     * @return a Jackson Object mapper with {@link SwitchingBusinessEntityModule}
     *         and mocked {@link GeneralLoaderDao}
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public ObjectMapper configuredObjectMapper(final BusinessEntity... entities) {
        Check.notNullArgument(entities, "entities");

        generalLoaderDaoMockCounter++;
        GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class,
                "GeneralLoaderDaoMock" + generalLoaderDaoMockCounter);
        for (BusinessEntity entity : entities) {
            this.context.checking(new Expectations() {
                {
                    oneOf(generalLoaderDao).getByBusinessId(entity.getBusinessId(), entity.getClass());
                    will(returnValue(entity));
                }
            });
        }

        return configuredObjectMapper(generalLoaderDao);
    }   
    
    public static class MultiElementContainer {
        private String value1;

        private DemoBusinessEntity demoBusinessEntity;

        private String value2;

        public MultiElementContainer() {
            super();
        }

        public MultiElementContainer(String value1, DemoBusinessEntity demoBusinessEntity, String value2) {
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

        @Override
        public String toString() {
            return "DemoContainerClassEntity [value1=" + value1 + ", demoBusinessEntity=" + demoBusinessEntity
                    + ", value2=" + value2 + "]";
        }

    }

    public static class ListContainer {
        private List<DemoBusinessEntity> demoBusinessEntitys;

        public ListContainer() {
            super();
        }

        public ListContainer(List<DemoBusinessEntity> list) {
            this.demoBusinessEntitys = new ArrayList<>(list);
        }

        public List<DemoBusinessEntity> getDemoBusinessEntitys() {
            return demoBusinessEntitys;
        }

        public void setDemoBusinessEntitys(List<DemoBusinessEntity> list) {
            this.demoBusinessEntitys = list;
        }

        @Override
        public String toString() {
            return "ListContainer [demoBusinessEntitys=" + demoBusinessEntitys + "]";
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

        @Override
        public String toString() {
            return "GenericEntityWrapper [content=" + content + "]";
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

        @Override
        public String toString() {
            return "GenericBusinessIdWrapper [content=" + content + "]";
        }

    }
    
    @Test
    public void testDemoBusinessEntity() {
        DemoBusinessEntity entity = new DemoBusinessEntity(new BusinessId<>(123), "Hello World");
        
        final String entityJsonString = "{'content':{'businessId':'123', 'content':'Hello World'}}";        
        final String bidJsonString = "{'content':'123'}";        
                
        assertEntityWrapperSerialization(entity, entityJsonString);
        assertEntityWrapperDeserialization(entityJsonString, entity);
        
        assertBidWrapperSerialization(entity, bidJsonString);
        assertBidWrapperDeserialization(bidJsonString, entity, entity);
    }

    
    @Test
    public void testMultiElementContainer() {
        DemoBusinessEntity entity = new DemoBusinessEntity(new BusinessId<>(123), "Hello World");
        MultiElementContainer container = new MultiElementContainer("v1", entity, "v2");
        
        
        // @formatter:off
        final String entityJsonString = 
                  "{'content':"
                + "  {"
                + "     'value1':'v1',"
                + "     'demoBusinessEntity':{'businessId':'123', 'content':'Hello World'},"
                + "     'value2':'v2'"
                + "  }"
                + "}";        
        // @formatter:on
        final String bidJsonString = 
                  "{'content':"
                + "  {"
                + "     'value1':'v1',"
                + "     'demoBusinessEntity':'123',"
                + "     'value2':'v2'"
                + "  }"
                + "}";        
        // @formatter:on        
                
        assertEntityWrapperSerialization(container, entityJsonString);
        assertEntityWrapperDeserialization(entityJsonString, container);
        
        assertBidWrapperSerialization(container, bidJsonString);
        assertBidWrapperDeserialization(bidJsonString, container, entity);
    }
    

    @Test
    public void testList() {
        DemoBusinessEntity element1 = new DemoBusinessEntity(new BusinessId<>(123), "Hello World1");
        DemoBusinessEntity element2 = new DemoBusinessEntity(new BusinessId<>(456), "Hello World2");
        ListContainer listContainer = new ListContainer(Arrays.asList(element1, element2));
        
        // @formatter:off
        final String entityJsonString =
                "{'content':{'demoBusinessEntitys':["
                        + "{'businessId':'123', 'content':'Hello World1'},"
                        + "{'businessId':'456', 'content':'Hello World2'}"
                 + "]}}";
        // @formatter:on
        
        // @formatter:off
        final String bidJsonString =
                "{'content':{'demoBusinessEntitys':["
              + "    '123',                         "
              + "    '456'                          "
              + "]}}";
        // @formatter:on
                
        assertEntityWrapperSerialization(listContainer, entityJsonString);
        assertEntityWrapperDeserialization(entityJsonString, listContainer);
        
        assertBidWrapperSerialization(listContainer, bidJsonString);
        assertBidWrapperDeserialization(bidJsonString, listContainer, element1, element2);
    }
    
    
    /**
     * Perform a test to verify a JavaObject ({@code serializContent}) become serialized to the expected json
     * string ({@code expectedJsonString}) when it is wrapped in an container annotated with
     * {@code @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.ENTITY)}}
     * ({@link GenericEntityWrapper}).
     * 
     * @param <C> the type of the expected content
     * @param serializContent the content that is (wrapped with {@link GenericEntityWrapper}) serialized
     * @param expectedJsonString  the expected expectedJsonString
     */
    public <C> void assertEntityWrapperSerialization(final C serializContent, final String expectedJsonString) {
        Check.notNullArgument(serializContent, "serializContent");
        Check.notEmptyArgument(expectedJsonString, "expectedJsonString");
        
        String expectedJson = expectedJsonString.replace("'", "\"");
        asserIsValidJason(expectedJson);

        GenericEntityWrapper<C> enityMarkerContainer = new GenericEntityWrapper<>(serializContent);        

        try {
            String jsonResult = configuredObjectMapper().writeValueAsString(enityMarkerContainer);
            try {
                JSONAssert.assertEquals(expectedJson, jsonResult, JSONCompareMode.NON_EXTENSIBLE);
            } catch (JSONException e) {
                throw new RuntimeException("error while read json for JsonAssert, expectedJsonString: `"
                        + expectedJson + "`\n jsonResult: `" + jsonResult + "`", e);
            }
        } catch (IOException e) {
            throw new RuntimeException("error while write json, JavaObject: `" + enityMarkerContainer + "`", e);
        }
    }

    /**
     * Perform a test to verify the Json Deserialization for an Json String that
     * contains the full entity and and loaded into an container annotated with
     * {@code @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.ENTITY)}}
     * ({@link GenericEntityWrapper}) works as expected.
     * 
     * @param                 <C> the type of the expected content
     * @param jsonString      the json string (single quotes will be replaced by
     *                        double quotes)
     * @param expectedContent the expected content of the parse object
     */
    public <C> void assertEntityWrapperDeserialization(final String jsonString, final C expectedContent) {
        Check.notEmptyArgument(jsonString, "jsonString");
        Check.notNullArgument(expectedContent, "expectedContent");

        String json = jsonString.replace("'", "\"");
        asserIsValidJason(json);

        ObjectMapper jacksonMapper = configuredObjectMapper();
        try {
            JavaType deserializationType = jacksonMapper.getTypeFactory()
                    .constructParametricType(GenericEntityWrapper.class, expectedContent.getClass());

            GenericEntityWrapper<C> result = jacksonMapper.readValue(json, deserializationType);

            GenericEntityWrapper<C> expectedEntityWrapperContainer = new GenericEntityWrapper<>(expectedContent);
            assertThat(result).usingRecursiveComparison().ignoringAllOverriddenEquals()
                    .isEqualTo(expectedEntityWrapperContainer);

        } catch (IOException e) {
            throw new RuntimeException("error while parse json: `" + json + "`");
        }
    }

    /**
     * Perform a test to verify a JavaObject ({@code serializContent}) become serialized to the expected json
     * string ({@code expectedJsonString}) when it is wrapped in an container annoteted with
     * {@code @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.BUSINESS_ID)}}
     * ({@link GenericBusinessIdWrapper}).
     * 
     * @param <C> the type of the expected content
     * @param serializContent the content that is (wrapped with {@link GenericBusinessIdWrapper}) serialized
     * @param expectedJsonString  the expected expectedJsonString
     */
    public <C> void assertBidWrapperSerialization(final C serializContent, final String expectedJsonString) {
        Check.notNullArgument(serializContent, "serializContent");
        Check.notEmptyArgument(expectedJsonString, "expectedJsonString");
        
        String expectedJson = expectedJsonString.replace("'", "\"");
        asserIsValidJason(expectedJson);

        GenericBusinessIdWrapper<C> enityMarkerContainer = new GenericBusinessIdWrapper<>(serializContent);

        try {
            String jsonResult = configuredObjectMapper().writeValueAsString(enityMarkerContainer);
            try {
                JSONAssert.assertEquals(expectedJson, jsonResult, JSONCompareMode.NON_EXTENSIBLE);
            } catch (JSONException e) {
                throw new RuntimeException("error while read json for JsonAssert, expectedJsonString: `"
                        + expectedJson + "`\n jsonResult: `" + jsonResult + "`", e);
            }
        } catch (IOException e) {
            throw new RuntimeException("error while write json, JavaObject: `" + enityMarkerContainer + "`", e);
        }
    }

    /**
     * Perform a test to verify the Json Deserialization for an Json String that
     * contains the full entity and and loaded into an container annotated with
     * {@code @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.BUSINESS_ID)}}
     * ({@link GenericBusinessIdWrapper}) works as expected.
     * 
     * @param                       <C> the type of the expected content
     * @param jsonString            the json string (single quotes will be replaced
     *                              by double quotes)
     * @param expectedContent       the expected content of the parse object
     * @param generalLoaderEntities - entities returned by the general Load Dao
     */
    public <C> void assertBidWrapperDeserialization(final String jsonString, final C exprectedContent,
            final BusinessEntity<?>... generalLoaderEntities) {
        Check.notEmptyArgument(jsonString, "jsonString");
        Check.notNullArgument(exprectedContent, "expectedContent");
        Check.notNullArgument(generalLoaderEntities, "generalLoaderEntities");

        String json = jsonString.replace("'", "\"");
        asserIsValidJason(json);

        ObjectMapper jacksonMapper = configuredObjectMapper(generalLoaderEntities);
        try {
            JavaType deserializationType = jacksonMapper.getTypeFactory()
                    .constructParametricType(GenericBusinessIdWrapper.class, exprectedContent.getClass());

            GenericBusinessIdWrapper<C> result = jacksonMapper.readValue(json, deserializationType);

            GenericBusinessIdWrapper<C> expectedBusinessIdMarkerContainer = new GenericBusinessIdWrapper<>(
                    exprectedContent);
            assertThat(result).usingRecursiveComparison().ignoringAllOverriddenEquals()
                    .isEqualTo(expectedBusinessIdMarkerContainer);

        } catch (IOException e) {
            throw new RuntimeException("error while parse json: `" + json + "`");
        }
    }

    public boolean isValidJson(final String json) {
        Check.notEmptyArgument(json, "json");
        
        try {
            configuredObjectMapper().readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        } catch (IOException e) {
            throw new RuntimeException("error while parse json: `" + json + "`");
        }
    }

    public void asserIsValidJason(String json) {
        Check.notEmptyArgument(json, "json");
        
        try {
            configuredObjectMapper().readTree(json);
        } catch (JsonProcessingException e) {
            fail("String `%s` is no valid json: %s", json, e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("error while parse json: `" + json + "`");
        }
    }
}
