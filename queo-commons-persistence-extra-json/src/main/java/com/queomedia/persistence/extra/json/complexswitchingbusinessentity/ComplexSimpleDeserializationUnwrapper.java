package com.queomedia.persistence.extra.json.complexswitchingbusinessentity;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotImplementedCaseException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.extra.json.BusinessEntityModule;
import com.queomedia.persistence.extra.json.BusinessEntitySerializationMode;
import com.queomedia.persistence.extra.json.SwitchingAnnotationScanner;
import com.queomedia.persistence.extra.json.BusinessEntityModule.TypedBusinessEntityJsonDeserializer;

public class ComplexSimpleDeserializationUnwrapper<T extends BusinessEntity<T>>  extends JsonDeserializer<T> {
        
    /** Detect the mode how to serialize */
    private final SwitchingAnnotationScanner switchingAnnotationScanner;

    /** Deserializer used for {@link BusinessEntitySerializationMode#BUSINESS_ID} to deserialize just from the BusinessID. */
    private final BusinessEntityModule.TypedBusinessEntityJsonDeserializer<T> businessEntityJsonDeserializer;

    /**
     * Deserializer used for {@link BusinessEntitySerializationMode#ENTITY} to deserialize the complete entity.
     * Is used to obtain an unwrappingDeerializer (by {@link #unwrapper}) before use them.
     */
    private final JsonDeserializer<T> defaultSerializer;
    
    /**
     * The Name transfromer used to obtain an unwrappingSerializer from the {@link #defaultDeserializer}.
     */
    private final NameTransformer unwrapper;

    public ComplexSimpleDeserializationUnwrapper(final SwitchingAnnotationScanner switchingAnnotationScanner,
            final TypedBusinessEntityJsonDeserializer<T> businessEntityJsonDeserializer,
            final JsonDeserializer<T> defaultSerializer,
            final NameTransformer unwrapper) {
        Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
        Check.notNullArgument(businessEntityJsonDeserializer, "businessEntityJsonDeserializer");
        Check.notNullArgument(defaultSerializer, "defaultSerializer");
        Check.notNullArgument(unwrapper, "unwrapper");
        
        this.switchingAnnotationScanner = switchingAnnotationScanner;
        this.businessEntityJsonDeserializer = businessEntityJsonDeserializer;
        this.defaultSerializer = defaultSerializer;
        this.unwrapper = unwrapper;
    }

//    @Override
//    public boolean isUnwrapping() {
//        return true;
//    }


    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        
        BusinessEntitySerializationMode mode = this.switchingAnnotationScanner
                .getSwitchDefinition(jp.getParsingContext());
        switch (mode) {
        case BUSINESS_ID:
            
            if (!jp.hasToken(JsonToken.START_OBJECT)) {
                throw new JsonParseException(jp, "START_OBJECT expected, but found:" + jp.currentToken());
            }
            for (String fieldName = jp.nextFieldName(); fieldName != null; fieldName = jp.nextFieldName()) {
                if (fieldName.equals("businessId")) {
                    jp.nextToken();
                    return this.businessEntityJsonDeserializer.deserialize(jp, ctxt);
                }
            }
            return null;
        case ENTITY:
            return this.defaultSerializer.unwrappingDeserializer(unwrapper).deserialize(jp, ctxt);
        default:
            throw new NotImplementedCaseException(mode);
        }
    }

}
