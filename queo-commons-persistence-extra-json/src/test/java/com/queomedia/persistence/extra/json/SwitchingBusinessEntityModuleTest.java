package com.queomedia.persistence.extra.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
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

    public static ObjectMapper configuredObjectMapper(GeneralLoaderDao generalLoaderDao) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(
                new Jdk8Module(),
                new SwitchingBusinessEntityModule(generalLoaderDao),
                new BusinessEntityOmitIdModule(),
                new BusinessIdModule()
                );
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

    /**
     * A demo entity only used in this test. Attention: this class must not have
     * a @Entity annotation (in order to prevent Hibernate from creating a table for
     * it)
     */
    static class DemoBusinessEntity extends BusinessEntity<DemoBusinessEntity> {

        /** Auto generated id for serializing. */
        private static final long serialVersionUID = 1292573538205615577L;

        private String content;

        @Deprecated
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

    static class MultiElementContainer {
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

    static class OptionalContainer {
        private Optional<DemoBusinessEntity> demoBusinessEntity;

        public OptionalContainer() {
            super();
        }

        public OptionalContainer(Optional<DemoBusinessEntity> demoBusinessEntity) {
            this.demoBusinessEntity = demoBusinessEntity;
        }

        public Optional<DemoBusinessEntity> getDemoBusinessEntity() {
            return demoBusinessEntity;
        }

        public void setDemoBusinessEntity(Optional<DemoBusinessEntity> demoBusinessEntity) {
            this.demoBusinessEntity = demoBusinessEntity;
        }

        @Override
        public String toString() {
            return "OptionalContainer [demoBusinessEntity=" + demoBusinessEntity + "]";
        }
    }

    static class ListContainer {
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

    static class MapEntityKeyContainer {
        private Map<DemoBusinessEntity, Integer> intByEntity;

        public MapEntityKeyContainer() {
            super();
        }

        public MapEntityKeyContainer(Map<DemoBusinessEntity, Integer> map) {
            this.intByEntity = new HashMap<>(map);
        }

        public Map<DemoBusinessEntity, Integer> getIntByEntity() {
            return intByEntity;
        }

        public void setIntByEntity(Map<DemoBusinessEntity, Integer> intByEntity) {
            this.intByEntity = intByEntity;
        }

        @Override
        public String toString() {
            return "ListContainer [demoBusinessEntitys=" + intByEntity + "]";
        }
    }

    static class MapEntityValueContainer {
        private Map<Integer, DemoBusinessEntity> demoBusinessEntitys;

        public MapEntityValueContainer() {
            super();
        }

        public MapEntityValueContainer(Map<Integer, DemoBusinessEntity> map) {
            this.demoBusinessEntitys = new HashMap<>(map);
        }

        public Map<Integer, DemoBusinessEntity> getDemoBusinessEntitys() {
            return demoBusinessEntitys;
        }

        public void setDemoBusinessEntitys(Map<Integer, DemoBusinessEntity> map) {
            this.demoBusinessEntitys = map;
        }

        @Override
        public String toString() {
            return "ListContainer [demoBusinessEntitys=" + demoBusinessEntitys + "]";
        }
    }

    static class JsonUnwrappedContainer {

        @JsonUnwrapped
        private DemoBusinessEntity demoBusinessEntity;

        private String value;

        public JsonUnwrappedContainer() {
            super();
        }

        public JsonUnwrappedContainer(DemoBusinessEntity demoBusinessEntity, String value) {
            this.demoBusinessEntity = demoBusinessEntity;
            this.value = value;
        }

        public DemoBusinessEntity getDemoBusinessEntity() {
            return demoBusinessEntity;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "DemoContainerClassEntity [demoBusinessEntity=" + demoBusinessEntity + ", value=" + value + "]";
        }
    }

    @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.ENTITY)
    static class GenericEntityWrapper<T> {
        /** can be null */
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
    static class GenericBusinessIdWrapper<T> {
        /** can be null */
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
    public void testNull() {
        DemoBusinessEntity nullEntity = null;

        final String entityJsonString = "{'content':null}";
        final String bidJsonString = "{'content':null}";

        assertEntityWrapperSerialization(nullEntity, entityJsonString);
        assertEntityWrapperDeserialization(entityJsonString, nullEntity, DemoBusinessEntity.class);

        assertBidWrapperSerialization(nullEntity, bidJsonString);
        assertBidWrapperDeserialization(bidJsonString, nullEntity, DemoBusinessEntity.class);
    }

    @Test
    public void testOptional_present() {
        DemoBusinessEntity entity = new DemoBusinessEntity(new BusinessId<>(123), "Hello World");
        OptionalContainer optionalContainer = new OptionalContainer(Optional.of(entity));

        final String entityJsonString = "{'content':{'demoBusinessEntity':{'businessId':'123', 'content':'Hello World'}}}";
        final String bidJsonString = "{'content':{'demoBusinessEntity':'123'}}";

        assertEntityWrapperSerialization(optionalContainer, entityJsonString);
        assertEntityWrapperDeserialization(entityJsonString, optionalContainer);

        assertBidWrapperSerialization(optionalContainer, bidJsonString);
        assertBidWrapperDeserialization(bidJsonString, optionalContainer, entity);
    }

    @Test
    public void testOptional_empty() {
        OptionalContainer optionalContainer = new OptionalContainer(Optional.empty());

        final String entityJsonString = "{'content':{'demoBusinessEntity':null}}";
        final String bidJsonString = "{'content':{'demoBusinessEntity':null}}";

        assertEntityWrapperSerialization(optionalContainer, entityJsonString);
        assertEntityWrapperDeserialization(entityJsonString, optionalContainer);

        assertBidWrapperSerialization(optionalContainer, bidJsonString);
        assertBidWrapperDeserialization(bidJsonString, optionalContainer);
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
        final String bidJsonString = "{'content':" + "  {" + "     'value1':'v1'," + "     'demoBusinessEntity':'123',"
                + "     'value2':'v2'" + "  }" + "}";
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

    @Ignore("Map Keys are currently not supported")
    @Test
    public void testMapEntityKey() {
        DemoBusinessEntity element1 = new DemoBusinessEntity(new BusinessId<>(123), "Hello World1");
        DemoBusinessEntity element2 = new DemoBusinessEntity(new BusinessId<>(456), "Hello World2");
        Map<DemoBusinessEntity, Integer> map = new HashMap<>();
        map.put(element1, 1);
        map.put(element2, 2);
        MapEntityKeyContainer mapContainer = new MapEntityKeyContainer(map);

        // @formatter:off
        final String entityJsonString =
                   "{'content':{'intByEntity':                         "
                 + "   {                                                       "
                 + "     '1' : {'businessId':'123', 'content':'Hello World1'}, "
                 + "     '2' : {'businessId':'456', 'content':'Hello World2'}  "
                 + "   }                                                       "
                 + "}}                                                         ";
        // @formatter:on

        // @formatter:off
        final String bidJsonString =
                "{'content':{'intByEntity':   "
              + "   {                                 "
              + "      '123' : '1',                   "
              + "      '456' : '2'                    "
              + "   }                                 "
              + "}}                                   ";
        // @formatter:on

        assertEntityWrapperSerialization(mapContainer, entityJsonString);
        assertEntityWrapperDeserialization(entityJsonString, mapContainer);

        assertBidWrapperSerialization(mapContainer, bidJsonString);
        assertBidWrapperDeserialization(bidJsonString, mapContainer, element1, element2);
    }

    @Test
    public void testMapEntityValue() {
        DemoBusinessEntity element1 = new DemoBusinessEntity(new BusinessId<>(123), "Hello World1");
        DemoBusinessEntity element2 = new DemoBusinessEntity(new BusinessId<>(456), "Hello World2");
        Map<Integer, DemoBusinessEntity> map = new HashMap<>();
        map.put(1, element1);
        map.put(2, element2);
        MapEntityValueContainer mapContainer = new MapEntityValueContainer(map);

        // @formatter:off
        final String entityJsonString =
                   "{'content':{'demoBusinessEntitys':                         "
                 + "   {                                                       "
                 + "     '1' : {'businessId':'123', 'content':'Hello World1'}, "
                 + "     '2' : {'businessId':'456', 'content':'Hello World2'}  "
                 + "   }                                                       "
                 + "}}                                                         ";
        // @formatter:on

        // @formatter:off
        final String bidJsonString =
                "{'content':{'demoBusinessEntitys':   "
              + "   {                                 "
              + "      '1' : '123',                   "
              + "      '2' : '456'                    "
              + "   }                                 "
              + "}}                                   ";
        // @formatter:on

        assertEntityWrapperSerialization(mapContainer, entityJsonString);
        assertEntityWrapperDeserialization(entityJsonString, mapContainer);

        assertBidWrapperSerialization(mapContainer, bidJsonString);
        assertBidWrapperDeserialization(bidJsonString, mapContainer, element1, element2);
    }
    
    @Test
    public void testJsonUnwrappedContainer() {
        DemoBusinessEntity entity = new DemoBusinessEntity(new BusinessId<>(123), "Hello World");
        JsonUnwrappedContainer container = new JsonUnwrappedContainer(entity, "something");

        // @formatter:off
        final String entityJsonString = 
                  "{'content':"
                + "  {                                              "
                + "     'businessId':'123',                         "
                + "     'content':'Hello World',                    "
                + "     'value':'something'                         "
                + "  }                                              "
                + "}";        
        // @formatter:on
        final String bidJsonString = 
                  "{'content':                                        " 
                + "  {                                                " 
                + "     'businessId':'123',                           "
                + "     'value':'something'                           " 
                + "  }                                                " 
                + "}";
        // @formatter:on        

        assertEntityWrapperSerialization(container, entityJsonString);
        assertEntityWrapperDeserialization(entityJsonString, container);

        //https://michael-simons.github.io/simple-meetup/unwrapping-custom-jackson-serializer ? 
        //https://stackoverflow.com/questions/14714328/jackson-how-to-add-custom-property-to-the-json-without-modifying-the-pojo
        assertBidWrapperSerialization(container, bidJsonString);
        assertBidWrapperDeserialization(bidJsonString, container, entity);
    }
    

    /**
     * Perform a test to verify a JavaObject ({@code serializContent}) become
     * serialized to the expected json string ({@code expectedJsonString}) when it
     * is wrapped in an container annotated with
     * {@code @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.ENTITY)}}
     * ({@link GenericEntityWrapper}).
     * 
     * @param                    <C> the type of the expected content
     * @param serializContent    the content that is (wrapped with
     *                           {@link GenericEntityWrapper}) serialized
     * @param expectedJsonString the expected expectedJsonString
     */
    public <C> void assertEntityWrapperSerialization(final C serializContent, final String expectedJsonString) {
        // serializContent can be null
        Check.notEmptyArgument(expectedJsonString, "expectedJsonString");

        String expectedJson = expectedJsonString.replace("'", "\"");
        asserIsValidJason(expectedJson);

        GenericEntityWrapper<C> enityMarkerContainer = new GenericEntityWrapper<>(serializContent);

        try {
            String jsonResult = configuredObjectMapper().writeValueAsString(enityMarkerContainer);
            try {
                System.out.println(jsonResult);
                JSONAssert.assertEquals(expectedJson, jsonResult, JSONCompareMode.NON_EXTENSIBLE);
            } catch (JSONException e) {
                throw new RuntimeException("error while read json for JsonAssert, expectedJsonString: `" + expectedJson
                        + "`\n jsonResult: `" + jsonResult + "`", e);
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
     * @param C               the type of the expected content
     * @param jsonString      the json string (single quotes will be replaced by
     *                        double quotes)
     * @param expectedContent the expected content of the parse object - can be null
     * @param contentClass    the type of expectedContent as well as {@code <C>}
     */
    public <C> void assertEntityWrapperDeserialization(final String jsonString, final C expectedContent,
            final Class<C> contentClass) {
        Check.notEmptyArgument(jsonString, "jsonString");
        // expectedContent can be null
        if (expectedContent != null) {
            Check.argumentInstanceOf(expectedContent, contentClass, "expectedContent");
        }
        Check.notNullArgument(contentClass, "contentClass");

        String json = jsonString.replace("'", "\"");
        asserIsValidJason(json);

        ObjectMapper jacksonMapper = configuredObjectMapper();
        try {
            JavaType deserializationType = jacksonMapper.getTypeFactory()
                    .constructParametricType(GenericEntityWrapper.class, contentClass);

            GenericEntityWrapper<C> result = jacksonMapper.readValue(json, deserializationType);

            GenericEntityWrapper<C> expectedEntityWrapperContainer = new GenericEntityWrapper<>(expectedContent);
            assertThat(result).usingRecursiveComparison().ignoringAllOverriddenEquals()
                    .isEqualTo(expectedEntityWrapperContainer);

        } catch (IOException e) {
            throw new RuntimeException("error while parse json: `" + json + "`");
        }
    }

    /**
     * Convenience method for
     * {@link #assertEntityWrapperDeserialization(String, Object, Class)} useable if
     * {@code expectedContent} is not null.
     * 
     * Perform a test to verify the Json Deserialization for an Json String that
     * contains the full entity and and loaded into an container annotated with
     * {@code @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.ENTITY)}}
     * ({@link GenericEntityWrapper}) works as expected.
     * 
     * @param C               the type of the expected content
     * @param jsonString      the json string (single quotes will be replaced by
     *                        double quotes)
     * @param expectedContent the expected content of the parse object - must not be
     *                        null
     */
    @SuppressWarnings("unchecked")
    public <C> void assertEntityWrapperDeserialization(final String jsonString, final C expectedContent) {
        Check.notEmptyArgument(jsonString, "jsonString");
        Check.notNullArgument(expectedContent, "expectedContent");

        assertEntityWrapperDeserialization(jsonString, expectedContent, (Class<C>) expectedContent.getClass());
    }

    /**
     * Perform a test to verify a JavaObject ({@code serializContent}) become
     * serialized to the expected json string ({@code expectedJsonString}) when it
     * is wrapped in an container annoteted with
     * {@code @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.BUSINESS_ID)}}
     * ({@link GenericBusinessIdWrapper}).
     * 
     * @param C                  the type of the expected content
     * @param serializContent    the content that is (wrapped with
     *                           {@link GenericBusinessIdWrapper}) serialized
     * @param expectedJsonString the expected expectedJsonString
     */
    public <C> void assertBidWrapperSerialization(final C serializContent, final String expectedJsonString) {
        // serializContent can be null
        Check.notEmptyArgument(expectedJsonString, "expectedJsonString");

        String expectedJson = expectedJsonString.replace("'", "\"");
        asserIsValidJason(expectedJson);

        GenericBusinessIdWrapper<C> enityMarkerContainer = new GenericBusinessIdWrapper<>(serializContent);

        try {
            String jsonResult = configuredObjectMapper().writeValueAsString(enityMarkerContainer);
            System.out.println(jsonResult);
            try {
                JSONAssert.assertEquals(expectedJson, jsonResult, JSONCompareMode.NON_EXTENSIBLE);
            } catch (JSONException e) {
                throw new RuntimeException("error while read json for JsonAssert, expectedJsonString: `" + expectedJson
                        + "`\n jsonResult: `" + jsonResult + "`", e);
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
     * @param C                     the type of the expected content
     * @param jsonString            the json string (single quotes will be replaced
     *                              by double quotes)
     * @param expectedContent       the expected content of the parse object - can
     *                              be null
     * @param contentClass          the type of expectedContent as well as
     *                              {@code <C>}
     * @param generalLoaderEntities - entities returned by the general Load Dao
     */
    public <C> void assertBidWrapperDeserialization(final String jsonString, final C expectedContent,
            final Class<C> contentClass, final BusinessEntity<?>... generalLoaderEntities) {
        Check.notEmptyArgument(jsonString, "jsonString");
        // expectedContent can be null
        if (expectedContent != null) {
            Check.argumentInstanceOf(expectedContent, contentClass, "expectedContent");
        }
        Check.notNullArgument(generalLoaderEntities, "generalLoaderEntities");

        String json = jsonString.replace("'", "\"");
        asserIsValidJason(json);

        ObjectMapper jacksonMapper = configuredObjectMapper(generalLoaderEntities);
        try {
            JavaType deserializationType = jacksonMapper.getTypeFactory()
                    .constructParametricType(GenericBusinessIdWrapper.class, contentClass);

            GenericBusinessIdWrapper<C> result = jacksonMapper.readValue(json, deserializationType);

            GenericBusinessIdWrapper<C> expectedBusinessIdMarkerContainer = new GenericBusinessIdWrapper<>(
                    expectedContent);
            assertThat(result).usingRecursiveComparison().ignoringAllOverriddenEquals()
                    .isEqualTo(expectedBusinessIdMarkerContainer);

        } catch (IOException e) {
            throw new RuntimeException("error while parse json: `" + json + "`");
        }
    }

    /**
     * Convenience method for
     * {@link #assertBidWrapperDeserialization(String, Object, Class, BusinessEntity...)}
     * useable if {@code expectedContent} is not null.
     * 
     * @param C                     the type of the expected content
     * @param jsonString            the json string (single quotes will be replaced
     *                              by double quotes)
     * @param expectedContent       the expected content of the parse object - must
     *                              not be null
     * @param generalLoaderEntities - entities returned by the general Load Dao
     */
    @SuppressWarnings("unchecked")
    public <C> void assertBidWrapperDeserialization(final String jsonString, final C expectedContent,
            final BusinessEntity<?>... generalLoaderEntities) {
        Check.notEmptyArgument(jsonString, "jsonString");
        Check.notNullArgument(expectedContent, "expectedContent");
        Check.notNullArgument(generalLoaderEntities, "generalLoaderEntities");

        assertBidWrapperDeserialization(jsonString, expectedContent, (Class<C>) expectedContent.getClass(),
                generalLoaderEntities);
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
