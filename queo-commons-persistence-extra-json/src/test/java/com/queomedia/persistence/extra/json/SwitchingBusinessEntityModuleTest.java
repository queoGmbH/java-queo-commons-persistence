package com.queomedia.persistence.extra.json;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
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
public class SwitchingBusinessEntityModuleTest {

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
        
        
        private String content;

        /**
         * Simple, empty constructor.
         * @param businessId businessId of the new entity.
         */
        public DemoBusinessEntity(final BusinessId<DemoBusinessEntity> businessId) {
            super(businessId);
            this.content = "Hello World";
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
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
        mapper.registerModules(
                new SwitchingBusinessEntityModule(this.context.mock(GeneralLoaderDao.class)),
                new BusinessEntityOmitIdModule(),
                new BusinessIdModule()
                );
        

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
        mapper.registerModule(new SwitchingBusinessEntityModule(generalLoaderDao));

        DemoBusinessEntity result = mapper.readValue(jsonString, DemoBusinessEntity.class);

        /* then: the json string is just the business id json-string */
        assertEquals(businessEntity, result);
    }
    
    /**
     * Scenario: deserialize to a Business Entity.
     *
     * @throws Exception - no exception should not been thrown in this test case
     */
    @Test
    public void testDeserializerForBusinessEntity_content() throws Exception {
        /* given: a json string that represent a business id and the business entity */
        final BusinessId<DemoBusinessEntity> businessId = new BusinessId<>(123);
        final String jsonString = "{\"businessId\":\"123\",\"content\":\"Hello World\"}";
        final DemoBusinessEntity businessEntity = new DemoBusinessEntity(businessId);

        /* then: no invocation expected */
        final GeneralLoaderDao generalLoaderDao = this.context.mock(GeneralLoaderDao.class);
       

        /* when: using jackson with the BussinessEntiyModule to deserialize the Business entity */
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SwitchingBusinessEntityModule(generalLoaderDao));

        DemoBusinessEntity result = mapper.readValue(jsonString, DemoBusinessEntity.class);

        /* then: the json string is just the business id json-string */
        assertEquals(businessEntity, result);
    }

}
