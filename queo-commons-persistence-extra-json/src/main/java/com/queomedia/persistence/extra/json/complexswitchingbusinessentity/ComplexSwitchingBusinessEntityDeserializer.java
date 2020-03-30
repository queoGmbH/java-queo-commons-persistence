package com.queomedia.persistence.extra.json.complexswitchingbusinessentity;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.BeanAsArrayDeserializer;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.queomedia.commons.checks.Check;
import com.queomedia.commons.exceptions.NotImplementedCaseException;
import com.queomedia.persistence.BusinessEntity;
import com.queomedia.persistence.extra.json.BusinessEntitySerializationMode;
import com.queomedia.persistence.extra.json.SwitchingAnnotationScanner;
import com.queomedia.persistence.extra.json.BusinessEntityModule.TypedBusinessEntityJsonDeserializer;

// https://stackoverflow.com/questions/18313323/how-do-i-call-the-default-deserializer-from-a-custom-deserializer-in-jackson
public class ComplexSwitchingBusinessEntityDeserializer<T extends BusinessEntity<T>> extends BeanDeserializerBase
        implements ResolvableDeserializer {

    private static final long serialVersionUID = 6686258398018608306L;

    private final SwitchingAnnotationScanner switchingAnnotationScanner;

    private TypedBusinessEntityJsonDeserializer<T> typedBusinessEntityJsonDeserializer;

    private final BeanDeserializerBase defaultDeserializer;

    private final boolean unwarppedMode;

    public ComplexSwitchingBusinessEntityDeserializer(final SwitchingAnnotationScanner switchingAnnotationScanner,
            final TypedBusinessEntityJsonDeserializer<T> typedBusinessEntityJsonDeserializer,
            final BeanDeserializerBase defaultDeserializer) {
        super(defaultDeserializer);
        Check.notNullArgument(switchingAnnotationScanner, "switchingAnnotationScanner");

        this.switchingAnnotationScanner = switchingAnnotationScanner;
        this.typedBusinessEntityJsonDeserializer = typedBusinessEntityJsonDeserializer;
        this.defaultDeserializer = defaultDeserializer;

        this.unwarppedMode = false;
    }

    protected ComplexSwitchingBusinessEntityDeserializer(final ComplexSwitchingBusinessEntityDeserializer<T> src,
            final NameTransformer unwrapper) {
        super(src, unwrapper);

        this.switchingAnnotationScanner = src.switchingAnnotationScanner;
        this.typedBusinessEntityJsonDeserializer = src.typedBusinessEntityJsonDeserializer;
        this.defaultDeserializer = src.defaultDeserializer;

        this.unwarppedMode = true;
    }

    protected ComplexSwitchingBusinessEntityDeserializer(final ComplexSwitchingBusinessEntityDeserializer<T> src,
            final ObjectIdReader oir) {
        super(src, oir);

        this.switchingAnnotationScanner = src.switchingAnnotationScanner;
        this.typedBusinessEntityJsonDeserializer = src.typedBusinessEntityJsonDeserializer;
        this.defaultDeserializer = src.defaultDeserializer;

        this.unwarppedMode = false;
    }

    protected ComplexSwitchingBusinessEntityDeserializer(final ComplexSwitchingBusinessEntityDeserializer<T> src,
            final Set<String> ignorableProps) {
        super(src, ignorableProps);

        this.switchingAnnotationScanner = src.switchingAnnotationScanner;
        this.typedBusinessEntityJsonDeserializer = src.typedBusinessEntityJsonDeserializer;
        this.defaultDeserializer = src.defaultDeserializer;

        this.unwarppedMode = false;
    }

    //for what is this method used?
    @Override
    public Object deserializeFromObject(final JsonParser jp, final DeserializationContext ctxt) throws IOException {

        BusinessEntitySerializationMode mode = this.switchingAnnotationScanner
                .getSwitchDefinition(jp.getParsingContext());
        switch (mode) {
        case BUSINESS_ID: {
            if (this.unwarppedMode) {
                if (!jp.hasToken(JsonToken.START_OBJECT)) {
                    throw new JsonParseException(jp, "START_OBJECT expected, but found:" + jp.currentToken());
                }
                for (String fieldName = jp.nextFieldName(); fieldName != null; fieldName = jp.nextFieldName()) {
                    if (fieldName.equals("businessId")) {
                        jp.nextToken();
                        return this.typedBusinessEntityJsonDeserializer.deserialize(jp, ctxt);
                    }
                }
                return null;
            } else {
                return this.typedBusinessEntityJsonDeserializer.deserialize(jp, ctxt);
            }
        }
        case ENTITY: {
            return this.defaultDeserializer.deserializeFromObject(jp, ctxt);
        }
        default:
            throw new NotImplementedCaseException(mode);
        }
    }

    @Override
    protected Object _deserializeUsingPropertyBased(final JsonParser jp, final DeserializationContext ctxt)
            throws IOException {
        //for load by business id, it does not matter if the ctor must be used,
        //and for the default, there is no public _deserializeUsingPropertyBased method, but
        //BeanDeserializer.deserializeFromObject will forward it
        //and so one can reuse this.deserializeFromObject
        return deserializeFromObject(jp, ctxt);
    }

    @Override
    public Object deserialize(final JsonParser jp, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        BusinessEntitySerializationMode mode = this.switchingAnnotationScanner
                .getSwitchDefinition(jp.getParsingContext());
        switch (mode) {
        case BUSINESS_ID: {
            if (this.unwarppedMode) {
                if (!jp.hasToken(JsonToken.START_OBJECT)) {
                    throw new JsonParseException(jp, "START_OBJECT expected, but found:" + jp.currentToken());
                }
                for (String fieldName = jp.nextFieldName(); fieldName != null; fieldName = jp.nextFieldName()) {
                    if (fieldName.equals("businessId")) {
                        jp.nextToken();
                        return this.typedBusinessEntityJsonDeserializer.deserialize(jp, ctxt);
                    }
                }
                return null;
            } else {
                return this.typedBusinessEntityJsonDeserializer.deserialize(jp, ctxt);
            }
        }
        case ENTITY: {
            return this.defaultDeserializer.deserialize(jp, ctxt);
        }
        default:
            throw new NotImplementedCaseException(mode);
        }

    }

    /**
     * State marker we need in order to avoid infinite recursion for some cases
     * (not very clean, alas, but has to do for now)
     *
     * @since 2.9
     */
    private volatile transient NameTransformer _currentlyTransforming;

    @Override
    public JsonDeserializer<Object> unwrappingDeserializer(final NameTransformer transformer) {
        // code from BeanDeserializer.unwrappingDeserializer

        // bit kludgy but we don't want to accidentally change type; sub-classes
        // MUST override this method to support unwrapped properties...
        if (getClass() != ComplexSwitchingBusinessEntityDeserializer.class) {
            return this;
        }
        // 25-Mar-2017, tatu: Not clean at all, but for [databind#383] we do need
        //   to keep track of accidental recursion...
        if (this._currentlyTransforming == transformer) {
            return this;
        }
        this._currentlyTransforming = transformer;
        try {
            return new ComplexSwitchingBusinessEntityDeserializer<T>(this, transformer);
        } finally {
            this._currentlyTransforming = null;
        }
    }

    @Override
    public ComplexSwitchingBusinessEntityDeserializer<T> withObjectIdReader(final ObjectIdReader oir) {
        return new ComplexSwitchingBusinessEntityDeserializer<T>(this, oir);
    }

    @Override
    public ComplexSwitchingBusinessEntityDeserializer<T> withIgnorableProperties(final Set<String> ignorableProps) {
        return new ComplexSwitchingBusinessEntityDeserializer<T>(this, ignorableProps);
    }

    @Override
    protected BeanDeserializerBase asArrayDeserializer() {
        SettableBeanProperty[] props = this._beanProperties.getPropertiesInInsertionOrder();
        return new BeanAsArrayDeserializer(this, props);
    }

}
