package com.queomedia.persistence.extra.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.queomedia.persistence.BusinessId;

/**
 * Jackson {@link com.fasterxml.jackson.databind.Module} that register BusinessId Json Serializer/Deserializer.
 *
 * They will map a business id object to a single (string) value that is the bid.
 *
 * Example: A class with field {@code BusinessId<User> userBid}, will become
 * {@code "userBid":"123" }
 *
 */
public class BusinessIdModule extends SimpleModule {

    /**  The Constant serialVersionUID. */
    private static final long serialVersionUID = -6409061927576334108L;

    /**
     * Instantiates a new business id module and register the mapping.
     */
    public BusinessIdModule() {
        super("businessIdModule", new Version(1, 0, 0, null, "com.queomedia", null));

        this.addSerializer(BusinessId.class, new BusinessIdJsonSerializer());
        this.addDeserializer(BusinessId.class, new BusinessIdJsonDeserializer());
    }

    /**
     * The Class BusinessIdJsonSerializer.
     */
    @SuppressWarnings("rawtypes")
    static class BusinessIdJsonSerializer extends JsonSerializer<BusinessId> {

        /*
         * (non-Javadoc)
         *
         * @see com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object,
         * com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
         */
        @Override
        public void serialize(final BusinessId businessId, final JsonGenerator jgen, final SerializerProvider provider)
                throws IOException, JsonProcessingException {

            if (businessId == null) {
                jgen.writeNull();
            } else {
                jgen.writeString(businessId.getAsString());
            }
        }

    }

    /**
     * The Class BusinessIdJsonDeserializer.
     */
    @SuppressWarnings("rawtypes")
    static class BusinessIdJsonDeserializer extends JsonDeserializer<BusinessId> {

        /*
         * (non-Javadoc)
         *
         * @see com.fasterxml.jackson.databind.JsonDeserializer#deserialize(com.fasterxml.jackson.core.JsonParser,
         * com.fasterxml.jackson.databind.DeserializationContext)
         */
        @Override
        public BusinessId deserialize(final JsonParser jp, final DeserializationContext ctxt)
                throws IOException, JsonProcessingException {

            String bidString = jp.readValueAs(String.class);
            if (bidString == null) {
                return null;
            } else {
                return BusinessId.parse(bidString);
            }
        }

    }

}
