package com.queomedia.persistence.extra.json.switchingbusinessentity;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotImplementedCaseException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.extra.json.BusinessEntityModule;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntityJsonSerializer;

public class SimpleSerializationUnwrapper extends JsonSerializer<BusinessEntity> {

    /** Detect the mode how to serialize */
    private final SwitchingAnnotationScanner switchingAnnotationScanner;

    /** Serializer used for {@link BusinessEntitySerializationMode#BUSINESS_ID} to serialize just the BusinessID. */
    private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;

    /**
     * Serializer used for {@link BusinessEntitySerializationMode#ENTITY} to serialize the complete entity.
     * Is used to obtain an unwrappingSerializer (by {@link #unwrapper}) before use them.
     */
    private final JsonSerializer<Object> defaultSerializer;

    /**
     * The Name transfromer used to obtain an unwrappingSerializer from the {@link #defaultSerializer}.
     */
    private final NameTransformer unwrapper;

    public SimpleSerializationUnwrapper(final SwitchingAnnotationScanner switchingAnnotationScanner,
            final BusinessEntityJsonSerializer businessEntityJsonSerializer,
            final JsonSerializer<Object> defaultSerializer, final NameTransformer unwrapper) {
        Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
        Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");
        Check.notNullArgument(defaultSerializer, "defaultSerializer");
        Check.notNullArgument(unwrapper, "unwrapper");

        this.switchingAnnotationScanner = switchingAnnotationScanner;
        this.businessEntityJsonSerializer = businessEntityJsonSerializer;
        this.defaultSerializer = defaultSerializer;
        this.unwrapper = unwrapper;
    }

    @Override
    public void serialize(final BusinessEntity businessEntity, final JsonGenerator jgen,
            final SerializerProvider providers)
            throws IOException {
        BusinessEntitySerializationMode mode = this.switchingAnnotationScanner
                .getSwitchDefinition(jgen.getOutputContext());
        switch (mode) {
        case BUSINESS_ID:
            jgen.writeFieldName("businessId");
            this.businessEntityJsonSerializer.serialize(businessEntity, jgen, providers);
            return;
        case ENTITY:
            this.defaultSerializer.unwrappingSerializer(this.unwrapper).serialize(businessEntity, jgen, providers);
            return;
        default:
            throw new NotImplementedCaseException(mode);
        }
    }

}
