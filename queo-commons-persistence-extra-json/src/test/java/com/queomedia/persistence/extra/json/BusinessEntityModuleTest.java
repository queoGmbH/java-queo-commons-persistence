package com.queomedia.persistence.extra.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;
import com.queomedia.persistence.GeneralLoaderDao;

/**
 * Tests the BusinessEntityModule, which converts BusinessId-Strings in Json to
 * BusinessEntitys during deserialization and BusinessEntitys to Id-Strings during serialization.
 * @author Prantz
 *
 */
public class BusinessEntityModuleTest {

    /** JMock Context */
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    /**
     * A demo entity only used in this test.
     * Attention: this class must not have a @Entity annotation (in order to prevent Hibernate from creating
     * a table for it)
     */
    private static class DemoBusinessEntity extends BusinessEntity<DemoBusinessEntity> {

        /** Auto generated id for serializing.*/
        private static final long serialVersionUID = 1292573538205615577L;

        /**
         * Simple, empty constructor.
         * @param businessId businessId of the new entity.
         */
        public DemoBusinessEntity(final BusinessId<DemoBusinessEntity> businessId) {
            super(businessId);
        }

        @Override
        public String toString() {
            return "DemoBusinessEntity []";
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
        DemoBusinessEntity businessEntity = new DemoBusinessEntity(businessId);

        /* when: using jackson with the BussinessEntiyModule to serialize the Business entity */
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BusinessEntityModule(this.context.mock(GeneralLoaderDao.class)));

        String jsonString = mapper.writeValueAsString(businessEntity);

        /* then: the returned json string is just the business id json-string */
        assertEquals("\"" + businessId.getAsString() + "\"", jsonString);
    }

    /**
     * Scenario: deserialize to a Business Entity.
     *
     * @throws Exception - no exception should not been thrown in this test case
     */
    @Test
    public void testDeserializerForBusinessEntity() throws Exception {
        /* given: a json string that represent a business id and the business entity */
        final BusinessId<DemoBusinessEntity> businessId = new BusinessId<>(123);
        final String jsonString = '"' + businessId.getAsString() + '"';
        final DemoBusinessEntity businessEntity = new DemoBusinessEntity(businessId);

        /* then: the deserializer use the GeneralLoaderDao to load the entity by its business id */
        final GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class);
        this.context.checking(new Expectations() {
            {
                oneOf(generalLoaderDao).getByBusinessId(businessId, DemoBusinessEntity.class);
                will(returnValue(businessEntity));
            }
        });

        /* when: using jackson with the BussinessEntiyModule to deserialize the Business entity */
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BusinessEntityModule(generalLoaderDao));

        DemoBusinessEntity result = mapper.readValue(jsonString, DemoBusinessEntity.class);

        /* then: the json string is just the business id json-string */
        assertEquals(businessEntity, result);
    }

    public static class MapEntityKeyContainer {
        private Map<DemoBusinessEntity, Integer> map = new HashMap<>();

        public MapEntityKeyContainer() {
            super();
        }

        public MapEntityKeyContainer(final Map<DemoBusinessEntity, Integer> map) {
            this.map = map;
        }

        public Map<DemoBusinessEntity, Integer> getMap() {
            return this.map;
        }

        public void setMap(final Map<DemoBusinessEntity, Integer> map) {
            this.map = map;
        }

    }

    /**
     * Scenario: serialize a Map where the keys are Business Entityies
     *
     * @throws Exception - no exception should not been thrown in this test case
     */
    @Test
    public void testSerializerForBusinessEntityKey() throws Exception {
        DemoBusinessEntity element1 = new DemoBusinessEntity(new BusinessId<>(123));
        DemoBusinessEntity element2 = new DemoBusinessEntity(new BusinessId<>(456));
        Map<DemoBusinessEntity, Integer> map = new HashMap<>();
        map.put(element1, 1);
        map.put(element2, 2);
        MapEntityKeyContainer mapContainer = new MapEntityKeyContainer(map);

        // @formatter:off
        final String expectedJson =
                   "{'map': {        "
                 + "  '123' : 1,     "
                 + "  '456' : 2      "
                 + "}}               ";
        // @formatter:on

        /* when: using jackson with the BussinessEntiyModule to serialize the map of Business entity */
        final GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BusinessEntityModule(generalLoaderDao));

        String jsonResult = mapper.writeValueAsString(mapContainer);

        JSONAssert.assertEquals(expectedJson, jsonResult, JSONCompareMode.NON_EXTENSIBLE);
    }

    /**
     * Scenario: serialize a Map where the keys are Business Entityies
     *
     * @throws Exception - no exception should not been thrown in this test case
     */
    @Test
    public void testDeserializerForBusinessEntityKey() throws Exception {
        DemoBusinessEntity element1 = new DemoBusinessEntity(new BusinessId<>(123));
        DemoBusinessEntity element2 = new DemoBusinessEntity(new BusinessId<>(456));
        Map<DemoBusinessEntity, Integer> map = new HashMap<>();
        map.put(element1, 1);
        map.put(element2, 2);
        MapEntityKeyContainer expectedMapContainer = new MapEntityKeyContainer(map);

        // @formatter:off
        final String jsonString =(
                   "{'map': {        "
                 + "  '123' : 1,     "
                 + "  '456' : 2      "
                 + "}}               ").replaceAll("'", "\"");
        // @formatter:on

        final GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class);
        ObjectMapper mapper = new ObjectMapper();
        this.context.checking(new Expectations() {
            {
                oneOf(generalLoaderDao).getByBusinessId(element1.getBusinessId(), DemoBusinessEntity.class);
                will(returnValue(element1));

                oneOf(generalLoaderDao).getByBusinessId(element2.getBusinessId(), DemoBusinessEntity.class);
                will(returnValue(element2));
            }
        });
        mapper.registerModule(new BusinessEntityModule(generalLoaderDao));

        /* when: using jackson with the BussinessEntiyModule to deserialize to a map of Business entity */
        MapEntityKeyContainer result = mapper.readValue(jsonString, MapEntityKeyContainer.class);

        assertThat(result).usingRecursiveComparison().ignoringAllOverriddenEquals().isEqualTo(expectedMapContainer);
    }

    public static class ListEntityKeyContainer {
        private List<DemoBusinessEntity> list = new ArrayList<>();

        public ListEntityKeyContainer() {
            super();
        }

        public ListEntityKeyContainer(final List<DemoBusinessEntity> list) {
            this.list = list;
        }

        public List<DemoBusinessEntity> getList() {
            return this.list;
        }

        public void setList(final List<DemoBusinessEntity> list) {
            this.list = list;
        }

        @Override
        public String toString() {
            return "ListEntityKeyContainer [list=" + this.list + "]";
        }

    }

    /**
     * Scenario: serialize a Map where the keys are Business Entityies
     *
     * @throws Exception - no exception should not been thrown in this test case
     */
    @Test
    public void testSerializerForCollection() throws Exception {
        DemoBusinessEntity element1 = new DemoBusinessEntity(new BusinessId<>(123));
        DemoBusinessEntity element2 = new DemoBusinessEntity(new BusinessId<>(456));
        List<DemoBusinessEntity> list = new ArrayList<>();
        list.add(element1);
        list.add(element2);

        ListEntityKeyContainer listContainer = new ListEntityKeyContainer(list);

        // @formatter:off
        final String expectedJson =
                   "{'list': ['123', '456']}";
        // @formatter:on

        /* when: using jackson with the BussinessEntiyModule to serialize the list of Business entity */
        final GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new BusinessEntityModule(generalLoaderDao));

        String jsonResult = mapper.writeValueAsString(listContainer);
        System.out.println(jsonResult);

        JSONAssert.assertEquals(expectedJson, jsonResult, JSONCompareMode.NON_EXTENSIBLE);
    }

    /**
     * Scenario: serialize a Map where the keys are Business Entityies
     *
     * @throws Exception - no exception should not been thrown in this test case
     */
    @Test
    public void testDeserializerForCollection() throws Exception {
        DemoBusinessEntity element1 = new DemoBusinessEntity(new BusinessId<>(123));
        DemoBusinessEntity element2 = new DemoBusinessEntity(new BusinessId<>(456));
        List<DemoBusinessEntity> list = new ArrayList<>();
        list.add(element1);
        list.add(element2);

        ListEntityKeyContainer expectedListContainer = new ListEntityKeyContainer(list);

        // @formatter:off
        final String jsonString =
                   "{'list': ['123', '456']}".replace('\'', '"');
        // @formatter:on

        final GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class);
        ObjectMapper mapper = new ObjectMapper();
        this.context.checking(new Expectations() {
            {
                oneOf(generalLoaderDao).getByBusinessId(element1.getBusinessId(), DemoBusinessEntity.class);
                will(returnValue(element1));

                oneOf(generalLoaderDao).getByBusinessId(element2.getBusinessId(), DemoBusinessEntity.class);
                will(returnValue(element2));
            }
        });
        mapper.registerModule(new BusinessEntityModule(generalLoaderDao));

        /* when: using jackson with the BussinessEntiyModule to deserialize to a list of Business entity */
        ListEntityKeyContainer result = mapper.readValue(jsonString, ListEntityKeyContainer.class);
        assertThat(result).usingRecursiveComparison().ignoringAllOverriddenEquals().isEqualTo(expectedListContainer);
    }

    /**
     * Scenario: serialize a Map where the keys are Business Entityies
     *
     * @throws Exception - no exception should not been thrown in this test case
     */
    @Test
    public void testDeserializerForCollectionWithoutContainer() throws Exception {
        DemoBusinessEntity element1 = new DemoBusinessEntity(new BusinessId<>(123));
        DemoBusinessEntity element2 = new DemoBusinessEntity(new BusinessId<>(456));

        // @formatter:off
        final String jsonString =
                   "['123', '456']".replace('\'', '"');
        // @formatter:on

        final GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class);
        ObjectMapper mapper = new ObjectMapper();
        this.context.checking(new Expectations() {
            {
                oneOf(generalLoaderDao).getByBusinessId(element1.getBusinessId(), DemoBusinessEntity.class);
                will(returnValue(element1));

                oneOf(generalLoaderDao).getByBusinessId(element2.getBusinessId(), DemoBusinessEntity.class);
                will(returnValue(element2));
            }
        });
        mapper.registerModule(new BusinessEntityModule(generalLoaderDao));

        /* when: using jackson with the BussinessEntiyModule to deserialize to a list of Business entity */

        JavaType deserializationType = mapper.getTypeFactory().constructParametricType(List.class,
                DemoBusinessEntity.class);
        List<DemoBusinessEntity> result = mapper.readValue(jsonString, deserializationType);

        assertThat(result).containsExactly(element1, element2);
    }

}
