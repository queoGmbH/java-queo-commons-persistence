package com.queomedia.persistence.extra.json;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.BusinessId;

public class BusinessEntityOmitIdModuleTest {

    /**
     * Test that "id", "hibernateId", "hibernateIdValue" and "new" does not become part of the JSON object.
     * @exception IOException no exception expected
     */
    @Test
    public void testSerialize() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new BusinessIdModule());
        objectMapper.registerModule(new BusinessEntityOmitIdModule());

        assertEquals("{\"businessId\":\"123\",\"title\":\"test\"}",
                objectMapper.writeValueAsString(new DemoBusinessEntity("test", new BusinessId<>(123))));
    }

    /** Demo class for {@link BusinessEntityOmitIdModuleTest#testSerialize()} test. */
    private static class DemoBusinessEntity extends BusinessEntity<DemoBusinessEntity> {

        private static final long serialVersionUID = -1940730689245089458L;

        private String title;

        public DemoBusinessEntity(final String title, final BusinessId<DemoBusinessEntity> bid) {
            super(bid);
            this.title = title;
        }

        @SuppressWarnings("unused")
        public String getTitle() {
            return this.title;
        }
    }

}
