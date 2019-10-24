package com.queomedia.persistence.extra.json;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;

public class BusinessIdModuleTest {

    @Test
    public void testSerialize() throws JsonGenerationException, JsonMappingException, IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new BusinessIdModule());

        assertEquals("\"123\"", objectMapper.writeValueAsString(new BusinessId<>(123)));
    }

    /**
     * Test serialize_with complex.
     *
     * @throws Exception no exception expected in this test
     */
    @Test
    public void testSerialize_withComplex() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new BusinessIdModule());

        String jsonResult = objectMapper
                .writeValueAsString(new TestContainer(new BusinessId<User>(1234), "Hallo Welt"));
        JSONAssert.assertEquals("{\"userBid\":\"1234\",\"text\":\"Hallo Welt\"}", jsonResult, JSONCompareMode.STRICT);
    }

    @Test
    public void testSerialize_withNull() throws JsonGenerationException, JsonMappingException, IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new BusinessIdModule());

        assertEquals("null", objectMapper.writeValueAsString(null));
    }

    /**
     * Test serialize_with complex with null.
     *
     * @throws Exception no exception expected in this test
     */
    @Test
    public void testSerialize_withComplexWithNull() throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new BusinessIdModule());

        String jsonResult = objectMapper.writeValueAsString(new TestContainer(null, "Hallo Welt"));
        JSONAssert.assertEquals("{\"userBid\":null,\"text\":\"Hallo Welt\"}", jsonResult, JSONCompareMode.STRICT);
    }

    @Test
    public void testDeserialize() throws JsonGenerationException, JsonMappingException, IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new BusinessIdModule());

        assertEquals(new BusinessId<>(123), objectMapper.readValue("\"123\"", BusinessId.class));
    }

    @Test
    public void testDeserialize_withComplex() throws JsonGenerationException, JsonMappingException, IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new BusinessIdModule());

        TestContainer expected = new TestContainer(new BusinessId<User>(1234), "Hallo Welt");
        assertEquals(expected,
                objectMapper.readValue("{\"userBid\":\"1234\",\"text\":\"Hallo Welt\"}", TestContainer.class));

    }

    @Test
    public void testDeserialize_withNull() throws JsonGenerationException, JsonMappingException, IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new BusinessIdModule());

        assertEquals(null, objectMapper.readValue("null", BusinessId.class));
    }

    @Test
    public void testDeserialize_withComplexWithNull()
            throws JsonGenerationException, JsonMappingException, IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new BusinessIdModule());

        TestContainer expected = new TestContainer(null, "Hallo Welt");
        assertEquals(expected,
                objectMapper.readValue("{\"userBid\":null,\"text\":\"Hallo Welt\"}", TestContainer.class));

    }

    static class TestContainer {
        public BusinessId<User> userBid;

        public String text;

        public TestContainer() {
            super();
        }

        public TestContainer(final BusinessId<User> userBid, final String text) {
            this.userBid = userBid;
            this.text = text;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((this.text == null) ? 0 : this.text.hashCode());
            result = (prime * result) + ((this.userBid == null) ? 0 : this.userBid.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TestContainer other = (TestContainer) obj;
            if (this.text == null) {
                if (other.text != null) {
                    return false;
                }
            } else if (!this.text.equals(other.text)) {
                return false;
            }
            if (this.userBid == null) {
                if (other.userBid != null) {
                    return false;
                }
            } else if (!this.userBid.equals(other.userBid)) {
                return false;
            }
            return true;
        }

    }

    /**
     * The type User.
     */
    private static class User extends BusinessEntity<User> {

        /**
         * Instantiates a new User.
         *
         * @param userBid the user bid
         */
        public User(final BusinessId<User> userBid) {
            super(userBid);
        }
    }
}
