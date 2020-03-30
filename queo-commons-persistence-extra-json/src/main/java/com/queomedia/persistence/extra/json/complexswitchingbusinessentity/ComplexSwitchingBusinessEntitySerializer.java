package com.queomedia.persistence.extra.json.complexswitchingbusinessentity;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.BeanAsArraySerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.UnwrappingBeanSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotImplementedCaseException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.extra.json.BusinessEntityModule;
import com.queomedia.persistence.extra.json.BusinessEntitySerializationMode;
import com.queomedia.persistence.extra.json.SwitchingAnnotationScanner;
import com.queomedia.persistence.extra.json.BusinessEntityModule.BusinessEntityJsonSerializer;

/**
 * Jackson Serializer that switch between BusinessEntityJsonSerializer to serialize just the business ids, and the normal
 * {@code defaultSerializer} that serialize the complete entity.
 *
 * @author engelmann
 */
public class ComplexSwitchingBusinessEntitySerializer extends BeanSerializerBase {

    private static final long serialVersionUID = 369005311052139994L;

    /** Detect the mode how to serialize */
    private final SwitchingAnnotationScanner switchingAnnotationScanner;

    /** Serializer used for {@link BusinessEntitySerializationMode#BUSINESS_ID} to serialize just the BusinessID. */
    private final BusinessEntityModule.BusinessEntityJsonSerializer businessEntityJsonSerializer;

    /** Serializer used for {@link BusinessEntitySerializationMode#ENTITY} to serialize the complete entity. */
    private final BeanSerializerBase defaultSerializer;

    /**
     * Standard constructor.
     *
     * @param switchingAnnotationScanner the switching annotation scanner
     * @param businessEntityJsonSerializer the business entity json serializer
     * @param defaultSerializer the default serializer
     */
    public ComplexSwitchingBusinessEntitySerializer(final SwitchingAnnotationScanner switchingAnnotationScanner,
            final BusinessEntityJsonSerializer businessEntityJsonSerializer,
            final BeanSerializerBase defaultSerializer) {
        super(defaultSerializer);

        Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");
        Check.notNullArgument(businessEntityJsonSerializer, "businessEntityJsonSerializer");
        Check.notNullArgument(defaultSerializer, "defaultSerializer");

        this.switchingAnnotationScanner = switchingAnnotationScanner;
        this.businessEntityJsonSerializer = businessEntityJsonSerializer;
        this.defaultSerializer = defaultSerializer;
    }

    protected ComplexSwitchingBusinessEntitySerializer(final ComplexSwitchingBusinessEntitySerializer src,
            final ObjectIdWriter objectIdWriter) {
        super(src, objectIdWriter);

        this.switchingAnnotationScanner = src.switchingAnnotationScanner;
        this.businessEntityJsonSerializer = src.businessEntityJsonSerializer;
        this.defaultSerializer = src.defaultSerializer;
    }

    protected ComplexSwitchingBusinessEntitySerializer(final ComplexSwitchingBusinessEntitySerializer src,
            final Set<String> toIgnore) {
        super(src, toIgnore);

        this.switchingAnnotationScanner = src.switchingAnnotationScanner;
        this.businessEntityJsonSerializer = src.businessEntityJsonSerializer;
        this.defaultSerializer = src.defaultSerializer;
    }

    protected ComplexSwitchingBusinessEntitySerializer(final ComplexSwitchingBusinessEntitySerializer src,
            final ObjectIdWriter objectIdWriter, final Object filterId) {
        super(src, objectIdWriter, filterId);

        this.switchingAnnotationScanner = src.switchingAnnotationScanner;
        this.businessEntityJsonSerializer = src.businessEntityJsonSerializer;
        this.defaultSerializer = src.defaultSerializer;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void serialize(final Object businessEntity, final JsonGenerator jgen, final SerializerProvider provider)
            throws IOException {

        BusinessEntitySerializationMode mode = this.switchingAnnotationScanner
                .getSwitchDefinition(jgen.getOutputContext());
        switch (mode) {
        case BUSINESS_ID:
            this.businessEntityJsonSerializer.serialize((BusinessEntity) businessEntity, jgen, provider);
            return;
        case ENTITY:
            this.defaultSerializer.serialize(businessEntity, jgen, provider);
            return;
        default:
            throw new NotImplementedCaseException(mode);
        }
    }

    @Override
    public JsonSerializer<Object> unwrappingSerializer(final NameTransformer transformer) {
        return new UnwrappingBeanSerializer(this, transformer) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void serializeFields(final Object bean, final JsonGenerator jgen,
                    final SerializerProvider provider)
                    throws IOException {
                BusinessEntitySerializationMode mode = ComplexSwitchingBusinessEntitySerializer.this.switchingAnnotationScanner
                        .getSwitchDefinition(jgen.getOutputContext());
                switch (mode) {
                case BUSINESS_ID:
                    jgen.writeFieldName("businessId");
                    ComplexSwitchingBusinessEntitySerializer.this.businessEntityJsonSerializer
                            .serialize(((BusinessEntity) bean), jgen, provider);
                    return;
                case ENTITY:
                    super.serializeFields(bean, jgen, provider);
                    return;
                default:
                    throw new NotImplementedCaseException(mode);
                }
            }
        };
    }

    @Override
    public BeanSerializerBase withObjectIdWriter(final ObjectIdWriter objectIdWriter) {
        return new ComplexSwitchingBusinessEntitySerializer(this, objectIdWriter);
    }

    @Override
    protected BeanSerializerBase withIgnorals(final Set<String> toIgnore) {
        return new ComplexSwitchingBusinessEntitySerializer(this, toIgnore);
    }

    @Override
    protected BeanSerializerBase asArraySerializer() {
        /*
         * Cannot:
         *
         * - have Object Id (may be allowed in future) - have "any getter" - have
         * per-property filters
         */
        if ((this._objectIdWriter == null) && (this._anyGetterWriter == null) && (this._propertyFilterId == null)) {
            return new BeanAsArraySerializer(this);
        }
        // already is one, so:
        return this;
    }

    @Override
    public BeanSerializerBase withFilterId(final Object filterId) {
        return new ComplexSwitchingBusinessEntitySerializer(this, this._objectIdWriter, filterId);
    }

}
