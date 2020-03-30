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
import com.queomedia.persistence.extra.json.SwitchingBusinessEntityModule;

public class SwitchingBusinessEntityModuleTest {

    /** JMock Context */
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    public static ObjectMapper configuredObjectMapper(final GeneralLoaderDao generalLoaderDao) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(new Jdk8Module(),
                new SwitchingBusinessEntityModule(generalLoaderDao,
                        BusinessEntitySerializationMode.ENTITY,
                        true,
                        true));
        return mapper;
    }

    private int generalLoaderDaoMockCounter = 0;

    /**
     * Build a Jackson Object Mapper with {@link SwitchingBusinessEntityModule}
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

        this.generalLoaderDaoMockCounter++;
        GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class,
                "GeneralLoaderDaoMock" + this.generalLoaderDaoMockCounter);
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
            return this.content;
        }

        @Override
        public String toString() {
            return "DemoBusinessEntity [content=" + this.content + "]";
        }
    }

    static class MultiElementContainer {
        private String value1;

        private DemoBusinessEntity demoBusinessEntity;

        private String value2;

        public MultiElementContainer() {
            super();
        }

        public MultiElementContainer(final String value1, final DemoBusinessEntity demoBusinessEntity,
                final String value2) {
            this.value1 = value1;
            this.demoBusinessEntity = demoBusinessEntity;
            this.value2 = value2;
        }

        public String getValue1() {
            return this.value1;
        }

        public DemoBusinessEntity getDemoBusinessEntity() {
            return this.demoBusinessEntity;
        }

        public String getValue2() {
            return this.value2;
        }

        @Override
        public String toString() {
            return "DemoContainerClassEntity [value1=" + this.value1 + ", demoBusinessEntity=" + this.demoBusinessEntity
                    + ", value2=" + this.value2 + "]";
        }
    }

    static class OptionalContainer {
        private Optional<DemoBusinessEntity> demoBusinessEntity;

        public OptionalContainer() {
            super();
        }

        public OptionalContainer(final Optional<DemoBusinessEntity> demoBusinessEntity) {
            this.demoBusinessEntity = demoBusinessEntity;
        }

        public Optional<DemoBusinessEntity> getDemoBusinessEntity() {
            return this.demoBusinessEntity;
        }

        public void setDemoBusinessEntity(final Optional<DemoBusinessEntity> demoBusinessEntity) {
            this.demoBusinessEntity = demoBusinessEntity;
        }

        @Override
        public String toString() {
            return "OptionalContainer [demoBusinessEntity=" + this.demoBusinessEntity + "]";
        }
    }

    static class ListContainer {
        private List<DemoBusinessEntity> demoBusinessEntitys;

        public ListContainer() {
            super();
        }

        public ListContainer(final List<DemoBusinessEntity> list) {
            this.demoBusinessEntitys = new ArrayList<>(list);
        }

        public List<DemoBusinessEntity> getDemoBusinessEntitys() {
            return this.demoBusinessEntitys;
        }

        public void setDemoBusinessEntitys(final List<DemoBusinessEntity> list) {
            this.demoBusinessEntitys = list;
        }

        @Override
        public String toString() {
            return "ListContainer [demoBusinessEntitys=" + this.demoBusinessEntitys + "]";
        }
    }

    static class MapEntityKeyContainer {
        private Map<DemoBusinessEntity, Integer> intByEntity;

        public MapEntityKeyContainer() {
            super();
        }

        public MapEntityKeyContainer(final Map<DemoBusinessEntity, Integer> map) {
            this.intByEntity = new HashMap<>(map);
        }

        public Map<DemoBusinessEntity, Integer> getIntByEntity() {
            return this.intByEntity;
        }

        public void setIntByEntity(final Map<DemoBusinessEntity, Integer> intByEntity) {
            this.intByEntity = intByEntity;
        }

        @Override
        public String toString() {
            return "ListContainer [demoBusinessEntitys=" + this.intByEntity + "]";
        }
    }

    static class MapEntityValueContainer {
        private Map<Integer, DemoBusinessEntity> demoBusinessEntitys;

        public MapEntityValueContainer() {
            super();
        }

        public MapEntityValueContainer(final Map<Integer, DemoBusinessEntity> map) {
            this.demoBusinessEntitys = new HashMap<>(map);
        }

        public Map<Integer, DemoBusinessEntity> getDemoBusinessEntitys() {
            return this.demoBusinessEntitys;
        }

        public void setDemoBusinessEntitys(final Map<Integer, DemoBusinessEntity> map) {
            this.demoBusinessEntitys = map;
        }

        @Override
        public String toString() {
            return "ListContainer [demoBusinessEntitys=" + this.demoBusinessEntitys + "]";
        }
    }

    static class JsonUnwrappedContainer {

        @JsonUnwrapped
        private DemoBusinessEntity demoBusinessEntity;

        private String value;

        public JsonUnwrappedContainer() {
            super();
        }

        public JsonUnwrappedContainer(final DemoBusinessEntity demoBusinessEntity, final String value) {
            this.demoBusinessEntity = demoBusinessEntity;
            this.value = value;
        }

        public DemoBusinessEntity getDemoBusinessEntity() {
            return this.demoBusinessEntity;
        }

        public String getValue() {
            return this.value;
        }

        @Override
        public String toString() {
            return "DemoContainerClassEntity [demoBusinessEntity=" + this.demoBusinessEntity + ", value=" + this.value
                    + "]";
        }
    }

    @SwitchingBusinessEntityAnnotation(BusinessEntitySerializationMode.ENTITY)
    static class GenericEntityWrapper<T> {
        /** can be null */
        private T content;

        public GenericEntityWrapper() {
            super();
        }

        public GenericEntityWrapper(final T content) {
            this.content = content;
        }

        public T getContent() {
            return this.content;
        }

        public void setContent(final T content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "GenericEntityWrapper [content=" + this.content + "]";
        }

    }

    @SwitchingBusinessEntityAnnotation(BusinessEntitySerializationMode.BUSINESS_ID)
    static class GenericBusinessIdWrapper<T> {
        /** can be null */
        private T content;

        public GenericBusinessIdWrapper() {
            super();
        }

        public GenericBusinessIdWrapper(final T content) {
            this.content = content;
        }

        public T getContent() {
            return this.content;
        }

        public void setContent(final T content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "GenericBusinessIdWrapper [content=" + this.content + "]";
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
                   "{'content':{'intByEntity':        "
                 + "   {                              "
                 + "     '123' : 1,                   "
                 + "     '456' : 2                    "
                 + "   }                              "
                 + "}}                                ";
        // @formatter:on

        // @formatter:off
        final String bidJsonString =
                "{'content':{'intByEntity':   "
              + "   {                                 "
              + "      '123' : 1,                     "
              + "      '456' : 2                      "
              + "   }                                 "
              + "}}                                   ";
        // @formatter:on

        assertEntityWrapperSerialization(mapContainer, entityJsonString);
        assertEntityWrapperDeserialization(entityJsonString, mapContainer, element1, element2);

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
        final String bidJsonString = "{'content':                     "
                + "  {                                                "
                + "     'businessId':'123',                           "
                + "     'value':'something'                           "
                + "  }                                                " + "}";
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
     * @param <C>                the type of the expected content
     * @param serializContent    the content that is (wrapped with {@link GenericEntityWrapper}) serialized
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
                JSONAssert.assertEquals(expectedJson, jsonResult, JSONCompareMode.NON_EXTENSIBLE);
            } catch (JSONException e) {
                throw new RuntimeException(
                        "error while read json for JsonAssert, expectedJsonString: `" + expectedJson
                                + "`\n jsonResult: `" + jsonResult + "`",
                        e);
            }
        } catch (IOException e) {
            throw new RuntimeException("error while write json, JavaObject: `" + enityMarkerContainer + "`", e);
        }

        this.context.assertIsSatisfied();
    }

    /**
     * Perform a test to verify the Json Deserialization for an Json String that
     * contains the full entity and and loaded into an container annotated with
     * {@code @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.ENTITY)}}
     * ({@link GenericEntityWrapper}) works as expected.
     *
     * @param <C> the generic type
     * @param jsonString the json string (single quotes will be replaced by double quotes)
     * @param expectedContent the expected content of the parse object - can be null
     * @param contentClass the content class
     * @param generalLoaderEntities entities returned by the general Load Dao
     */
    public <C> void assertEntityWrapperDeserialization(final String jsonString, final C expectedContent,
            final Class<C> contentClass, final BusinessEntity<?>... generalLoaderEntities) {
        Check.notEmptyArgument(jsonString, "jsonString");
        // expectedContent can be null
        if (expectedContent != null) {
            Check.argumentInstanceOf(expectedContent, contentClass, "expectedContent");
        }
        Check.notNullArgument(contentClass, "contentClass");
        Check.notNullArgument(generalLoaderEntities, "generalLoaderEntities");

        String json = jsonString.replace("'", "\"");
        asserIsValidJason(json);

        ObjectMapper jacksonMapper = configuredObjectMapper(generalLoaderEntities);
        try {
            JavaType deserializationType = jacksonMapper.getTypeFactory()
                    .constructParametricType(GenericEntityWrapper.class, contentClass);

            GenericEntityWrapper<C> result = jacksonMapper.readValue(json, deserializationType);

            GenericEntityWrapper<C> expectedEntityWrapperContainer = new GenericEntityWrapper<>(expectedContent);
            assertThat(result).usingRecursiveComparison().ignoringAllOverriddenEquals()
                    .isEqualTo(expectedEntityWrapperContainer);

        } catch (IOException e) {
            throw new RuntimeException("error while parse json: `" + json + "`", e);
        }

        this.context.assertIsSatisfied();
    }

    /**
     * Convenience method for
     * {@link #assertEntityWrapperDeserialization(String, Object, Class, BusinessEntity...)} useable if
     * {@code expectedContent} is not null.
     *
     * Perform a test to verify the Json Deserialization for an Json String that
     * contains the full entity and and loaded into an container annotated with
     * {@code @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.ENTITY)}}
     * ({@link GenericEntityWrapper}) works as expected.
     *
     * @param <C>             the type of the expected content
     * @param jsonString      the json string (single quotes will be replaced by double quotes)
     * @param expectedContent the expected content of the parse object - must not be null
     * @param generalLoaderEntities entities returned by the general Load Dao
     */
    @SuppressWarnings("unchecked")
    public <C> void assertEntityWrapperDeserialization(final String jsonString, final C expectedContent,
            final BusinessEntity<?>... generalLoaderEntities) {
        Check.notEmptyArgument(jsonString, "jsonString");
        Check.notNullArgument(expectedContent, "expectedContent");

        assertEntityWrapperDeserialization(jsonString,
                expectedContent,
                (Class<C>) expectedContent.getClass(),
                generalLoaderEntities);
    }

    /**
     * Perform a test to verify a JavaObject ({@code serializContent}) become
     * serialized to the expected json string ({@code expectedJsonString}) when it
     * is wrapped in an container annotated with
     * {@code @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.BUSINESS_ID)}}
     * ({@link GenericBusinessIdWrapper}).
     *
     * @param <C>                  the type of the expected content
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
                throw new RuntimeException(
                        "error while read json for JsonAssert, expectedJsonString: `" + expectedJson
                                + "`\n jsonResult: `" + jsonResult + "`",
                        e);
            }
        } catch (IOException e) {
            throw new RuntimeException("error while write json, JavaObject: `" + enityMarkerContainer + "`", e);
        }

        this.context.assertIsSatisfied();
    }

    /**
     * Perform a test to verify the Json Deserialization for an Json String that
     * contains the full entity and and loaded into an container annotated with
     * {@code @SwitchingBusinessEntityAnnotation(BusinessEntitySerialization.BUSINESS_ID)}}
     * ({@link GenericBusinessIdWrapper}) works as expected.
     *
     * @param <C>                   the type of the expected content
     * @param jsonString            the json string (single quotes will be replaced by double quotes)
     * @param expectedContent       the expected content of the parse object - can be null
     * @param <C>ontentClass        the type of expectedContent as well as {@code <C>}
     * @param generalLoaderEntities entities returned by the general Load Dao
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
            throw new RuntimeException("error while parse json: `" + json + "`", e);
        }

        this.context.assertIsSatisfied();
    }

    /**
     * Convenience method for
     * {@link #assertBidWrapperDeserialization(String, Object, Class, BusinessEntity...)}
     * useable if {@code expectedContent} is not null.
     *
     * @param <C>                   the type of the expected content
     * @param jsonString            the json string (single quotes will be replaced by double quotes)
     * @param expectedContent       the expected content of the parse object - must not be null
     * @param generalLoaderEntities entities returned by the general Load Dao
     */
    @SuppressWarnings("unchecked")
    public <C> void assertBidWrapperDeserialization(final String jsonString, final C expectedContent,
            final BusinessEntity<?>... generalLoaderEntities) {
        Check.notEmptyArgument(jsonString, "jsonString");
        Check.notNullArgument(expectedContent, "expectedContent");
        Check.notNullArgument(generalLoaderEntities, "generalLoaderEntities");

        assertBidWrapperDeserialization(jsonString,
                expectedContent,
                (Class<C>) expectedContent.getClass(),
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

    public void asserIsValidJason(final String json) {
        Check.notEmptyArgument(json, "json");

        try {
            configuredObjectMapper().readTree(json);
        } catch (JsonProcessingException e) {
            fail("is no valid json: the string `%s` is no valid json: %s", json, e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("error while parse json: `" + json + "`");
        }
    }
}
